# Hardware Security: Car Rental
Repository for the Hardware Security project

## Introduction

This project contains the source for the Hardware Security project, a car rental system with the use of Java Smartcards.

## Requirements
* Java Smartcard
* Smartcard reader

## Installation

* Clone this repository
* For both Rental* folders, import the source in Eclipse. The RentalCarTerminal project is a normal Java 1.6 project, the RentalCarApplet a JavaCard project.
* For the RentalCarTerminal, add the JUnit 4 library and all JARs under the `lib/` folder.
* For the RentalCarApplet, set the applets AID. The First AID is `01 02 03 04 05 06 06`, the sub AID is `01 02 03 04 05 06 06 01`

When the project is ready, it can be tested via two ways

1. Start the class `com.rental.terminal.gui.MainWindow`
2. Start the class `com.rental.terminal.ProtocolDemo`
