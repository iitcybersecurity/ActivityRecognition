# Android GaitAuth Sample App

## Getting Started

### Build the app
The building process is very standard if you are building through Android studio, you can follow this [doc](https://developer.android.com/studio/run) which gives a good example.

### Using the app

You will need a UnifyID SDK Key to run the app. See the [GaitAuth Getting Started Guide](https://developer.unify.id/docs/gaitauth/)
for additional details about developing with GaitAuth and to create an SDK Key through the developer portal.

## Project Structure

```
app/src/main/java/id/unify/gaitauth_sample_app/
├── FeatureStore.java     # A simple file storage to store data for training
├── GaitAuthService.java  # Data collection for training and scoring all happen in this bound foreground service
├── MainActivity.java     # The only activity for this app, it internally changes fragment based on app state
├── Preferences.java      # App state storage
└── fragments             # Contains all fragments used by MainActivity
    ├── FeatureCollectionFragment.java
    ├── InfoDialog.java
    ├── ModelErrorFragment.java
    ├── ModelPendingFragment.java
    ├── ScoreFragment.java
    ├── SelectModelFragment.java
    └── TestingFragment.java
```

## Important Structures
This app has only one activity and displays a fragment as the main UI content based on the application state. You can find all its fragments in the `fragments` directory.


## Background Collection of Training Data

To train a model for a user, it is important to try to collect a broad range of representative samples of that user's behavior.
With this goal in mind, it tends to be helpful to collect data in the background to ensure that the app collects data when the user is
conducting their normal activities, not just when they are actively using the app.

This sample application uses a bound foreground service as a way to collect data even if the app is closed. The foreground service displays a persistent notification as a way to communicate to users if the app is collecting data. When the application is opened, it binds to the running service, when the app closes, it unbinds from the service. 

Collected training data is stored in a file storage `FeatureStore.java`. Collected data is first stored here and added to a model as the end-user clicks the "add features" button, the app then deletes the added data from this storage after the data is successfully added to the model. This is mainly to make sure we don't lose data when we can't add collected features to a model when we are offline. 

For scoring, the app uses the same bound foreground service as a way to collect data even if the app is in the background or closed.

## License

MIT License, see [LICENSE](./LICENSE).
