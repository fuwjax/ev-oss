/*
 * Copyright (C) 2015 fuwjax.org (info@fuwjax.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fuwjax.oss.sample;


import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by fuwjax on 2/15/15.
 */
public class SampleFieldInject {
    @Inject
    private int id;
    @Inject
    private String name;

    private SampleFieldInject(){
        // for injection
    }

    public SampleFieldInject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SampleFieldInject)){
            return false;
        }
        SampleFieldInject o = (SampleFieldInject)obj;
        return Objects.equals(getClass(), o.getClass()) && id == o.id && Objects.equals(name, o.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
