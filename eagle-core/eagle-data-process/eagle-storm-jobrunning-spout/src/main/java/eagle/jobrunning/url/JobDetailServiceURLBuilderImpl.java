/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eagle.jobrunning.url;

import eagle.jobrunning.common.JobConstants;

public class JobDetailServiceURLBuilderImpl implements ServiceURLBuilder {
	
	public String build(String... parameters) {
		// parameter[0] = baseUrl , parameter[1] = appID
		// {rmUrl}/proxy/application_xxx/ws/v1/mapreduce/jobs?anonymous=true
		return parameters[0] + JobConstants.V2_PROXY_PREFIX_URL + parameters[1] + JobConstants.V2_APP_DETAIL_URL + "?" + JobConstants.ANONYMOUS_PARAMETER;
	}
}
