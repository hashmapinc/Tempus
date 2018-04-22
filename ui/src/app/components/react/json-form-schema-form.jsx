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

import tempusArray from './json-form-array.jsx';
import tempusJavaScript from './json-form-javascript.jsx';
import tempusJson from './json-form-json.jsx';
import tempusHtml from './json-form-html.jsx';
import tempusCss from './json-form-css.jsx';
import tempusColor from './json-form-color.jsx'
import tempusRcSelect from './json-form-rc-select.jsx';
import tempusNumber from './json-form-number.jsx';
import tempusText from './json-form-text.jsx';
import Select from 'react-schema-form/lib/Select';
import Radios from 'react-schema-form/lib/Radios';
import tempusDate from './json-form-date.jsx';
import tempusImage from './json-form-image.jsx';
import tempusCheckbox from './json-form-checkbox.jsx';
import Help from 'react-schema-form/lib/Help';
import tempusFieldSet from './json-form-fieldset.jsx';

import _ from 'lodash';

class tempusSchemaForm extends React.Component {

    constructor(props) {
        super(props);

        this.mapper = {
            'number': tempusNumber,
            'text': tempusText,
            'password': tempusText,
            'textarea': tempusText,
            'select': Select,
            'radios': Radios,
            'date': tempusDate,
            'image': tempusImage,
            'checkbox': tempusCheckbox,
            'help': Help,
            'array': tempusArray,
            'javascript': tempusJavaScript,
            'json': tempusJson,
            'html': tempusHtml,
            'css': tempusCss,
            'color': tempusColor,
            'rc-select': tempusRcSelect,
            'fieldset': tempusFieldSet
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
export default tempusSchemaForm;
