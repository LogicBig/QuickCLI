package com.logicbig.cli.shell;

/**
 * Created by Joe on 11/13/2015.
 */
class OptionFlagObject extends Describable {

    protected OptionFlagObject(char flagName, String description) {
        super(Character.toString(flagName), description);

        if (!Character.isLetter(flagName)) {
            throw new IllegalArgumentException("The provided flagName has to be a letter of english alphabets: " + flagName);
        }
    }

    public char getFlgName() {
        return getName().charAt(0);
    }
}
