//
//  MainViewController.swift
//  iosApp
//
//  Created by MichelleRaouf on 01/08/2024.
//

import Foundation
import UIKit
import ComposeApp

class MainViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Ensure the compose view controller is not nil
        let composeViewController = MainKt.MainViewController()
        addChild(composeViewController)
        composeViewController.view.frame = view.bounds
        composeViewController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(composeViewController.view)
        composeViewController.didMove(toParent: self)
        
        // Check for updates
    }

  

}
