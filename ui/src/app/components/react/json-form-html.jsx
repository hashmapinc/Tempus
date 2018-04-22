/*
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import tempusAceEditor from './json-form-ace-editor.jsx';
import 'brace/mode/html';
import beautify from 'js-beautify';

const html_beautify = beautify.html;

class tempusHtml extends React.Component {

    constructor(props) {
        super(props);
        this.onTidyHtml = this.onTidyHtml.bind(this);
    }

    onTidyHtml(html) {
        return html_beautify(html, {indent_size: 4});
    }

    render() {
        return (
            <tempusAceEditor {...this.props} mode='html' onTidy={this.onTidyHtml} {...this.state}></tempusAceEditor>
        );
    }
}

export default tempusHtml;
