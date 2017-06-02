package com.bluelinelabs.conductor.demo.changehandler;

import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat;

public class ArcFadeMoveChangeHandlerCompat extends TransitionChangeHandlerCompat {

    public ArcFadeMoveChangeHandlerCompat() {
        super(new ArcFadeMoveChangeHandler(), new ArcFadeMoveChangeHandlerTransitionsEverywhere());
    }

}
