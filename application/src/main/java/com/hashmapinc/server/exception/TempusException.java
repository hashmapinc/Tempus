/**
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
package com.hashmapinc.server.exception;

public class TempusException extends Exception {

    private static final long serialVersionUID = 1L;

    private TempusErrorCode errorCode;

    public TempusException() {
        super();
    }

    public TempusException(TempusErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public TempusException(String message, TempusErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TempusException(String message, Throwable cause, TempusErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public TempusException(Throwable cause, TempusErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public TempusErrorCode getErrorCode() {
        return errorCode;
    }

}
