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
import './json-form.scss';

import React from 'react';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import tempusTheme from './styles/tempusTheme';
import tempusSchemaForm from './json-form-schema-form.jsx';

class ReactSchemaForm extends React.Component {

    getChildContext() {
        return {
            muiTheme: this.state.muiTheme
        };
    }

    constructor(props) {
        super(props);
        this.state = {
            muiTheme: getMuiTheme(tempusTheme)
        };
    }

    render () {
        if (this.props.form.length > 0) {
            return <tempusSchemaForm {...this.props} />;
        } else {
            return <div></div>;
        }
    }
}

ReactSchemaForm.propTypes = {
        schema: React.PropTypes.object,
        form: React.PropTypes.array,
        model: React.PropTypes.object,
        option: React.PropTypes.object,
        onModelChange: React.PropTypes.func,
        onColorClick: React.PropTypes.func
}

ReactSchemaForm.defaultProps = {
    schema: {},
    form: [ "*" ]
}

ReactSchemaForm.childContextTypes = {
        muiTheme: React.PropTypes.object
}

export default ReactSchemaForm;
