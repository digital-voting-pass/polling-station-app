# Digital Voting Pass App

[![platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)
[![Build Status](https://travis-ci.org/digital-voting-pass/digital-voting-pass-app.svg?branch=develop)](https://travis-ci.org/digital-voting-pass/digital-voting-pass-app) 
<a target="_blank" href="https://www.openhub.net/accounts/digitalVotingPass"><img alt='Open Hub profile for digitalVotingPass' border='0' height='15' src='https://www.openhub.net/accounts/digitalVotingPass/widgets/account_tiny?format=gif&amp;ref=sample' width='80'>
</a>


Part of a [Delft Unversity of Technology](https://www.tudelft.nl) bachelor's thesis about the digitalization of the voting pass for Dutch elections using **blockchain** and **machine readable travel documents**.

The scope of this project is limited to the [voting pass](https://nl.wikipedia.org/wiki/Stempas) only and forms one step towards the digitalization of the entire voting process. 

This app is intended for use at the polling station. An official scans a voter's travel document to verify and redeem the suffrage, which process is stored on the blockchain and can be verifed by anyone. After the suffrage is verified, a ballot is handed out and the voting process continues in a traditional way (by pencil and paper). 


<img  src="https://user-images.githubusercontent.com/2787511/27765839-d3b2f5ba-5ebd-11e7-9909-f0d76adb4524.gif" width="350" />

<a href="https://play.google.com/store/apps/details?id=com.digitalvotingpass.digitalvotingpass">
  <img  alt="Android app on Google Play" src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" />
</a>

## Getting Started

Import the root folder into your IDE (tested on Android Studio), then run project.

Connects to 188.226.149.56 (currently offline), which acts as a node in the digital voting pass blockchain network. See [digital-voting-pass-multichain](https://github.com/digital-voting-pass/digital-voting-pass-multichain) on how to setup your own node.

This server also hosts a blockchain explorer, which can be accesses here: [http://188.226.149.56:2750/](http://188.226.149.56:2750/) (currently offline).

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

See also the list of [contributors](https://github.com/digital-voting-pass/digital-voting-pass-app/contributors) who participated in this project.

## License

This project is licensed under the LGPL License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments
We would like to give special thanks to:
* Johan Pouwelse ([synctext](https://github.com/synctext)) for his guidance and blockchain expertise
* Milvum ([milvum.com](https://www.milvum.com)) for the resources they provided
* Anyone who's code was used for those great building blocks
