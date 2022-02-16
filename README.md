# GaitAuth 

[UnifyId GaithAuth](https://developer.unify.id/docs/gaitauth/)

This repository contains two projects:
- `cosine-api` is a simple Web API (written in Golang) used to store and compare GaitAuth features
- `gaitauth-sample-app-android` is an Android application project forked from [UnifyID repository](https://github.com/UnifyID/gaitauth-sample-app-android) and modified for our purpose: once initialized with SDK key, the application will load a fragment where the user can start retrieving features using GaitAuth SDK, send those features to backend for storing and comparision (done using Cosine simlarity).

For more details about the projects you can read `README.md` inside project's directory.

Inside the root directory of this project there is also an Android bundle ready for installation: `app.apk`.

Simple demo of the application:
![app-demo](app-demo.gif)