package com.teliacompany.tiberius.base.utils;

public final class ClassNameUtils {
    private ClassNameUtils() {
        //Not to be instantiated
    }

    /**
     * This will Handle TiberiusPrincepsPrimis (=tiberius-princeps-primis) but not TiberiusABSubscription. (it would be turned in to tiberius-a-b-subscription) Fix the day we get that case...
     * @param clazz class file
     * @return name of class in kebab-case
     */
    public static String simpleNameToKebabCase(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        final char[] nameChars = clazz.getSimpleName().toCharArray();
        stringBuilder.append(Character.toLowerCase(nameChars[0]));
        for(int i = 1; i < nameChars.length; i++) {
            final char c = nameChars[i];
            final char nc = Character.toLowerCase(c);
            if(Character.isUpperCase(c)) {
                stringBuilder.append('-').append(nc);
            } else {
                stringBuilder.append(nc);
            }
        }
        return stringBuilder.toString();
    }
}
