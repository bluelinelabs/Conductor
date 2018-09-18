[![Travis Build](https://travis-ci.org/bluelinelabs/Conductor.svg)](https://travis-ci.org/bluelinelabs/Conductor) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Conductor-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3361) [![Javadocs](http://javadoc.io/badge/com.bluelinelabs/conductor.svg)](http://javadoc.io/doc/com.bluelinelabs/conductor)

# Conductor

A small, yet full-featured framework that allows building View-based Android applications. Conductor provides a light-weight wrapper around standard Android Views that does just about everything you'd want:

|           |  Conductor  |
|-----------|-------------|
:tada: | Easy integration
:point_up: | Single Activity apps without using Fragments
:recycle: | Simple but powerful lifecycle management
:train: | Navigation and backstack handling
:twisted_rightwards_arrows: | Beautiful transitions between views
:floppy_disk: | State persistence
:phone: | Callbacks for onActivityResult, onRequestPermissionsResult, etc
:european_post_office: | MVP / MVVM / VIPER / MVC ready

Conductor is architecture-agnostic and does not try to force any design decisions on the developer. We here at BlueLine Labs tend to use either MVP or MVVM, but it would work equally well with standard MVC or whatever else you want to throw at it.

## Installation

```gradle
implementation 'com.bluelinelabs:conductor:2.1.5'

// If you want the components that go along with
// Android's support libraries (currently just a PagerAdapter):
implementation 'com.bluelinelabs:conductor-support:2.1.5'

// If you want RxJava lifecycle support:
implementation 'com.bluelinelabs:conductor-rxlifecycle:2.1.5'

// If you want RxJava2 lifecycle support:
implementation 'com.bluelinelabs:conductor-rxlifecycle2:2.1.5'

// If you want RxJava2 Autodispose support:
implementation 'com.bluelinelabs:conductor-autodispose:2.1.5'

// If you want Controllers that are Lifecycle-aware (architecture components):
implementation 'com.bluelinelabs:conductor-archlifecycle:2.1.5'
```

**SNAPSHOT**

Just use `2.1.6-SNAPSHOT` as your version number in any of the dependencies above and add the url to the snapshot repository:

```gradle
allprojects {
  repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
  }
}
```

## Components to Know

|             |  Conductor Components |
------|------------------------------
__Controller__ | The Controller is the View wrapper that will give you all of your lifecycle management features. Think of it as a lighter-weight and more predictable Fragment alternative with an easier to manage lifecycle.
__Router__ | A Router implements navigation and backstack handling for Controllers. Router objects are attached to Activity/containing ViewGroup pairs. Routers do not directly render or push Views to the container ViewGroup, but instead defer this responsibility to the ControllerChangeHandler specified in a given transaction.
__ControllerChangeHandler__ | ControllerChangeHandlers are responsible for swapping the View for one Controller to the View of another. They can be useful for performing animations and transitions between Controllers. Several default ControllerChangeHandlers are included.
__ControllerTransaction__ | Transactions are used to define data about adding Controllers. RouterControllerTransactions are used to push a Controller to a Router with specified ControllerChangeHandlers, while ChildControllerTransactions are used to add child Controllers.

## Getting Started

### Minimal Activity implementation

```java
public class MainActivity extends Activity {

    private Router router;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ViewGroup container = (ViewGroup) findViewById(R.id.controller_container);

        router = Conductor.attachRouter(this, container, savedInstanceState);
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new HomeController()));
        }
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

}
```

### Minimal Controller implementation

```java
public class HomeController extends Controller {

    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflater.inflate(R.layout.controller_home, container, false);
        ((TextView) view.findViewById(R.id.tv_title)).setText("Hello World");
        return view;
    }

}
```

### Sample Project

[Demo app](https://github.com/bluelinelabs/conductor/tree/master/demo) - Shows how to use all basic and most advanced functions of Conductor.

### Controller Lifecycle

The lifecycle of a Controller is significantly simpler to understand than that of a Fragment. A lifecycle diagram is shown below:

![Controller Lifecycle](docs/Controller%20Lifecycle.jpg)

## Advanced Topics

### Retain View Modes
`setRetainViewMode` can be called on a `Controller` with one of two values: `RELEASE_DETACH`, which will release the `Controller`'s view as soon as it is detached from the screen (saves memory), or `RETAIN_DETACH`, which will ensure that a `Controller` holds on to its view, even if it's not currently shown on the screen (good for views that are expensive to re-create).

### Custom Change Handlers
`ControllerChangeHandler` can be subclassed in order to perform different functions when changing between two `Controllers`. Two convenience `ControllerChangeHandler` subclasses are included to cover most basic needs: `AnimatorChangeHandler`, which will use an `Animator` object to transition between two views, and `TransitionChangeHandler`, which will use Lollipop's `Transition` framework for transitioning between views.

### Child Routers & Controllers
`getChildRouter` can be called on a `Controller` in order to get a nested `Router` into which child `Controller`s can be pushed. This enables creating advanced layouts, such as Master/Detail.

### RxJava Lifecycle
If the AutoDispose dependency has been added, there is a `ControllerScopeProvider` available that can be used along with the standard [AutoDispose library](https://github.com/uber/AutoDispose).

## Community Projects
The community has provided several helpful modules to make developing apps with Conductor even easier. Here's a collection of helpful libraries:

* [ConductorGlide](https://github.com/MkhytarMkhoian/ConductorGlide) - Adds Glide lifecycle support to Controllers
* [ConductorDialog](https://github.com/MkhytarMkhoian/ConductorDialog) - Adds a helpful DialogController (a Conductor version of DialogFragment)
* [Mosby-Conductor](https://github.com/sockeqwe/mosby-conductor) - A plugin to integrate Mosby, an MVP/MVI library


## License
```
Copyright 2016 BlueLine Labs, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
