/*
 * Copyright 2015 LogicBig.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logicbig.cli.text.table;

/**
 * @author Joe Khan
 */
abstract class TextTableColumn {
    private boolean rightJustify;
    private String leadingSpaces;
    private int displayWidth;
    private String format;

    public TextTableColumn(boolean rightJustify, int leadingSpaces) {
        this.rightJustify = rightJustify;
        this.leadingSpaces = leadingSpaces == 0 ? "" : leadingSpaces + "";
    }

    public boolean isRightJustify() {
        return rightJustify;
    }

    public String getLeadingSpaces() {
        return leadingSpaces;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public void buildFormat() {
        this.format = provideFormat();
    }

    public String getFormat() {
        return this.format;
    }

    protected abstract String provideFormat();
}