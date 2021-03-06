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
package eagle.security.hive.sensitivity;

import com.typesafe.config.Config;
import eagle.datastream.*;
import eagle.security.util.ExternalDataCache;
import eagle.security.util.ExternalDataJoiner;
import eagle.security.hive.entity.HiveResourceSensitivityAPIEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class HiveResourceSensitivityDataJoinExecutor extends JavaStormStreamExecutor2<String, Map> {
    private final static Logger LOG = LoggerFactory.getLogger(
            HiveResourceSensitivityDataJoinExecutor.class);
    private Config config;
    
    @Override
    public void prepareConfig(Config config) {
        this.config = config;       
    }

    @Override
    public void init() {
        // start hive resource data polling
        try {
            ExternalDataJoiner joiner = new ExternalDataJoiner(
                    HiveResourceSensitivityPollingJob.class, config);
            joiner.start();
        } catch(Exception ex){
            LOG.error("Fail to bring up quartz scheduler.", ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void flatMap(java.util.List<Object> input, Collector<Tuple2<String, Map>> outputCollector){
        @SuppressWarnings("unchecked")
        Map<String, Object> event = (Map<String, Object>)input.get(0);
        @SuppressWarnings("unchecked")
        Map<String, HiveResourceSensitivityAPIEntity> map =
                (Map<String, HiveResourceSensitivityAPIEntity>) ExternalDataCache
                        .getInstance()
                        .getJobResult(HiveResourceSensitivityPollingJob.class);
        LOG.info(">>>> event: " + event + " >>>> map: " + map);
        /**
         * Regex support for file directory.
         */

        /*
         * Split resource into a list.
         */
        String resource = (String)event.get("resource");
        List<String> resourceList = Arrays.asList(resource.split("\\s*,\\s*"));
        HiveResourceSensitivityAPIEntity sensitivityEntity = null;
        /*
         * Check if hive resource contains sensitive data.
         */
        for (String s : resourceList) {
            if (map != null) {
                sensitivityEntity = null;
                for (String r : map.keySet()) {
                    boolean isMatched = Pattern.matches(r, s);
                    if (isMatched) {
                        sensitivityEntity = map.get(r);
                        break;
                    }
                }
            }
            Map<String, Object> newEvent = new TreeMap<String, Object>(event);
            newEvent.put("sensitivityType", sensitivityEntity  == null ?
                    "NA" : sensitivityEntity.getSensitivityType());
            newEvent.put("resource", s);
            if(LOG.isDebugEnabled()) {
                LOG.debug("After hive resource sensitivity lookup: " + newEvent);
            }
            LOG.info("After hive resource sensitivity lookup: " + newEvent);
            outputCollector.collect(new Tuple2(
                    newEvent.get("user"),
//                    "hiveAccessLogStream",
                    newEvent));
        }
    }
}
