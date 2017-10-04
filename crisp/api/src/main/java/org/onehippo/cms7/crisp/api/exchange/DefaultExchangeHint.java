/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.cms7.crisp.api.exchange;

/**
 * Default {@link ExchangeHint} implementation.
 */
class DefaultExchangeHint implements ExchangeHint {

    private String methodName;
    private Object request;

    @Override
    public String getMethodName() {
        return methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Object getRequest() {
        return request;
    }

    void setRequest(Object request) {
        this.request = request;
    }

    @Override
    public Object getCacheKey() {
        StringBuilder sb = new StringBuilder(10);
        if (methodName != null) {
            sb.append("method=").append(methodName);
        }
        if (request != null) {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append("request=").append(request.toString());
        }
        return sb.toString();
    }

}
