package org.edushak.testspec

import org.edushak.testspec.util.Helper
import org.edushak.testspec.util.RestHelper

class TestSpecWorld {
    static TestSpecWorld currentWorld

    Binding binding
    void setBinding(Binding binding) {
        this.binding = binding
        if (!binding.hasVariable('_restCall')) {
            binding.setVariable('_restCall', RestHelper.&call)
        }
    }

    static noQuotes(Object str) {
        if (str instanceof CharSequence) {
            Helper.noQuotes(str)
        } else {
            str
        }
    }
    String normalizeValue(String value) {
        normalizeValue(value,binding)
    }

    static String normalizeValue(String value, Binding binding) {
        var(noQuotes(value) as String, binding)
    }

    Object var(String str) {
        var(str, binding)
    }
    static Object var(String str, Binding binding) {
        binding.hasVariable(str) ? binding.getVariable(str) : str
    }
}
