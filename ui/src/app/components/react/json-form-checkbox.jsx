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
import tempusBaseComponent from './json-form-base-component.jsx';
import Checkbox from 'material-ui/Checkbox';

class tempusCheckbox extends React.Component {
    render() {
        return (
            <Checkbox
                name={this.props.form.key.slice(-1)[0]}
                value={this.props.form.key.slice(-1)[0]}
                defaultChecked={this.props.value || false}
                label={this.props.form.title}
                disabled={this.props.form.readonly}
                onCheck={(e, checked) => {this.props.onChangeValidate(e)}}
                style={{paddingTop: '14px'}}
            />
        );
    }
}

export default tempusBaseComponent(tempusCheckbox);