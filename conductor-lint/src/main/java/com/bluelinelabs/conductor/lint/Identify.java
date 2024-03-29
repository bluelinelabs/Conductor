package com.bluelinelabs.conductor.lint;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

final class Identify {
    private Identify() {}

    static PsiElement byName(PsiNameIdentifierOwner psiNameIdentifierOwner) {
        if (psiNameIdentifierOwner.getNameIdentifier() != null) {
            return psiNameIdentifierOwner.getNameIdentifier();
        }
        return psiNameIdentifierOwner;
    }
}
