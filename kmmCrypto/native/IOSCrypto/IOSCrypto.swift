//
//  IOSCrypto.swift
//  IOSCrypto
//
//  Created by Michelle Raouf on 02/03/2025.
//

import Foundation

@objc public class IOSCrypto: NSObject {
    public typealias GetCompletion = (String?, Error?) -> Void
    public typealias GetDataCompletion = (Data?, Error?) -> Void

    public typealias SaveCompletion = (Error?) -> Void

    @objc public enum KeychainError: Int, Error {
        case unknown = -1
        case noData = 1
        case unexpectedData = 2

        public func description() -> String {
            switch self {
            case .unknown:
                return "Unknown error"
            case .noData:
                return "No data found"
            case .unexpectedData:
                return "Unexpected data found"
            }
        }
    }

    
    @objc public static func save(
           service: String,
           account: String,
           data: String,  // Now passing data as a String
           completion: @escaping SaveCompletion
       ) {
           // Convert the String to Data using utf8 encoding
           guard let dataAsData = data.data(using: .utf8) else {
               completion(KeychainError.unknown)  // Handle conversion failure
               return
           }
           
           let query: [String: AnyObject] = [
               kSecClass as String: kSecClassGenericPassword,
               kSecAttrService as String: service as AnyObject,
               kSecAttrAccount as String: account as AnyObject
           ]

           let attributesToUpdate: [String: AnyObject] = [
               kSecValueData as String: dataAsData as AnyObject
           ]

           let status = SecItemUpdate(query as CFDictionary, attributesToUpdate as CFDictionary)

           if status == errSecItemNotFound {
               // إذا لم يتم العثور على العنصر، قم بإضافته
               var newQuery = query
               newQuery[kSecValueData as String] = dataAsData as AnyObject  // التصحيح هنا

               let addStatus = SecItemAdd(newQuery as CFDictionary, nil)

               if addStatus != errSecSuccess {
                   completion(KeychainError.unknown)
               } else {
                   completion(nil)
               }
           } else if status != errSecSuccess {
               completion(KeychainError.unknown)
           } else {
               completion(nil)
           }
       }

    
    @objc public static func saveDataType(
        service: String,
        account: String,
        data: Data,
        completion: @escaping SaveCompletion
    ) {
        let query: [String: AnyObject] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service as AnyObject,
            kSecAttrAccount as String: account as AnyObject
        ]

        let attributesToUpdate: [String: AnyObject] = [
            kSecValueData as String: data as AnyObject
        ]

        let status = SecItemUpdate(query as CFDictionary, attributesToUpdate as CFDictionary)

        if status == errSecItemNotFound {
            // إذا لم يتم العثور على العنصر، قم بإضافته
            var newQuery = query
            newQuery[kSecValueData as String] = data as AnyObject
            let addStatus = SecItemAdd(newQuery as CFDictionary, nil)

            if addStatus != errSecSuccess {
                completion(KeychainError.unknown)
            } else {
                completion(nil)
            }
        } else if status != errSecSuccess {
            completion(KeychainError.unknown)
        } else {
            completion(nil)
        }
    }

    
    @objc public static func get(service: String, account: String, completion: @escaping GetCompletion) {
        do {
            let data = try retrieveData(service: service, account: account)
            let dataString = String(data: data, encoding: .utf8)
            completion(dataString, nil)
        } catch {
            completion(nil, error)
        }
    }
    
    @objc public static func getDataType(service: String, account: String, completion: @escaping GetDataCompletion) {
        do {
            let data = try retrieveData(service: service, account: account)
            completion(data, nil)
        } catch {
            completion(nil, error)
        }
    }
    
    
    private static func retrieveData(service: String, account: String) throws -> Data {
          let query: [String: AnyObject] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service as AnyObject,
            kSecAttrAccount as String: account as AnyObject,
            kSecReturnData as String: kCFBooleanTrue,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)
        
        switch status {
        case errSecSuccess:
            guard let data = item as? Data else {
                throw KeychainError.unexpectedData
            }
            return data
            
        case errSecItemNotFound:
            throw KeychainError.noData
            
        default:
            throw KeychainError.unknown
        }
    }
    
    @objc public static func deleteData(service: String, account: String) {
        let query: [String: AnyObject] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service as AnyObject,
            kSecAttrAccount as String: account as AnyObject
        ]
        
        SecItemDelete(query as CFDictionary)
    
    }
}
