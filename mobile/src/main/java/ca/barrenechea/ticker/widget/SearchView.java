/*
 * Copyright (C) 2014 Eduardo Barrenechea
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

package ca.barrenechea.ticker.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SearchView extends android.widget.SearchView {

    private OnExpandCollapseListener mListener;

    public interface OnExpandCollapseListener {
        public void onExpand();

        public void onCollapse();
    }

    public SearchView(Context context) {
        super(context);
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnExpandCollapseListener(OnExpandCollapseListener listener) {
        mListener = listener;
    }

    @Override
    public void onActionViewCollapsed() {
        super.onActionViewCollapsed();

        mListener.onCollapse();
    }

    @Override
    public void onActionViewExpanded() {
        super.onActionViewExpanded();

        mListener.onExpand();
    }
}
