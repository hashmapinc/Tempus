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
import { utils } from 'react-schema-form';

import TempusArray from './json-form-array.jsx';
import TempusJavaScript from './json-form-javascript.jsx';
import TempusJson from './json-form-json.jsx';
import TempusHtml from './json-form-html.jsx';
import TempusCss from './json-form-css.jsx';
import TempusColor from './json-form-color.jsx'
import TempusRcSelect from './json-form-rc-select.jsx';
import TempusNumber from './json-form-number.jsx';
import TempusText from './json-form-text.jsx';
import Select from 'react-schema-form/lib/Select';
import Radios from 'react-schema-form/lib/Radios';
import TempusDate from './json-form-date.jsx';
import TempusImage from './json-form-image.jsx';
import TempusCheckbox from './json-form-checkbox.jsx';
import Help from 'react-schema-form/lib/Help';
import TempusFieldSet from './json-form-fieldset.jsx';

import _ from 'lodash';

class TempusSchemaForm extends React.Component {

    constructor(props) {
        super(props);

        this.mapper = {
            'number': TempusNumber,
            'text': TempusText,
            'password': TempusText,
            'textarea': TempusText,
            'select': Select,
            'radios': Radios,
            'date': TempusDate,
            'image': TempusImage,
            'checkbox': TempusCheckbox,
            'help': Help,
            'array': TempusArray,
            'javascript': TempusJavaScript,
            'json': TempusJson,
            'html': TempusHtml,
            'css': TempusCss,
            'color': TempusColor,
            'rc-select': TempusRcSelect,
            'fieldset': TempusFieldSet
        };

        this.onChange = this.onChange.bind(this);
        this.onColorClick = this.onColorClick.bind(this);
    }

    onChange(key, val) {
        //console.log('SchemaForm.onChange', key, val);
        this.props.onModelChange(key, val);
    }

    onColorClick(event, key, val) {
        this.props.onColorClick(event, key, val);
    }

    builder(form, model, index, onChange, onColorClick, mapper) {
        var type = form.type;
        let Field = this.mapper[type];
        if(!Field) {
            console.log('Invalid field: \"' + form.key[0] + '\"!');
            return null;
        }
        if(form.condition && eval(form.condition) === false) {
            return null;
        }
        return <Field model={model} form={form} key={index} onChange={onChange} onColorClick={onColorClick} mapper={mapper} builder={this.builder}/>
    }

    render() {
        let merged = utils.merge(this.props.schema, this.props.form, this.props.ignore, this.props.option);
        let mapper = this.mapper;
        if(this.props.mapper) {
            mapper = _.merge(this.mapper, this.props.mapper);
        }
        let forms = merged.map(function(form, index) {
            return this.builder(form, this.props.model, index, this.onChange, this.onColorClick, mapper);
        }.bind(this));

        return (
            <div style={{width: '100%'}} className='SchemaForm'>{forms}</div>
        );
    }
}
export default TempusSchemaForm;
