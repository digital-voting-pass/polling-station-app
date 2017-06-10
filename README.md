# Digital voting pass app

[![platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![Build Status](https://travis-ci.org/digital-voting-pass/digital-voting-pass-app.svg?branch=develop)](https://travis-ci.org/digital-voting-pass/digital-voting-pass-app)


Part of the TU Delft Bachelor project about the digitalization of the voting pass for Dutch elections using blockchain and machine readable travel documents.
This app is used at the polling station by scanning a travel document to verify and redeem the suffrage, which is stored on the blockchain.

[<img src="https://user-images.githubusercontent.com/2787511/27002571-8d902bb8-4de5-11e7-94d5-da48a4209fdc.gif" width="350" />]()

## Getting Started

Import the root folder into your IDE (tested on Android Studio), then run project.

Connects to 188.226.149.56, which acts as a node in the digital voting pass blockchain network. See [digital-voting-pass-multichain](https://github.com/digital-voting-pass/digital-voting-pass-multichain) on how to setup your own node.

This server also hosts a blockchain explorer, which can be accesses here: [http://188.226.149.56:2750/](http://188.226.149.56:2750/).

## Built With

* [Android studio](https://developer.android.com/studio/index.html) - The IDE used
* [jMRTD](http://jmrtd.org/) - The library for reading the epassport chip
* [Tesseract](https://github.com/tesseract-ocr/tesseract) - OCR library for reading the MRZ
* [Bitcoinj](https://bitcoinj.github.io/) - Blockchain node library


## Authors

* **Wilko Meijer** - [wkmeijer](https://github.com/wkmeijer)
* **Daan Middendorp** - [landgenoot](https://github.com/landgenoot)
* **Jonathan Raes** - [jonathanraes](https://github.com/jonathanraes)
* **Rico Tubbing** - [klikooo](https://github.com/klikooo)

See also the list of [contributors](https://github.com/digital-voting-pass/digital-voting-pass-appgit/contributors) who participated in this project.

## License

This project is licensed under the LGPL License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc
