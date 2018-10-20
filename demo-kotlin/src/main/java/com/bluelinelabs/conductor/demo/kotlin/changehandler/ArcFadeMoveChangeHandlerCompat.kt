package com.bluelinelabs.conductor.demo.kotlin.changehandler

import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat

class ArcFadeMoveChangeHandlerCompat : TransitionChangeHandlerCompat {

    constructor() : super() {}

    constructor(vararg transitionNames: String) : super(ArcFadeMoveChangeHandler(*transitionNames), FadeChangeHandler())

}
