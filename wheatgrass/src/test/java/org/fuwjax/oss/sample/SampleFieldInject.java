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

import org.fuwjax.oss.util.ValueObject;

/**
 * Created by fuwjax on 2/15/15.
 */
public class SampleFieldInject extends ValueObject{
    @Inject
    private int id;
    @Inject
    private String name;

	private SampleFieldInject(){
		// for injection
		deferId(this::getClass, this::id, this::name);
	}

    public SampleFieldInject(int id, String name) {
    	this();
        this.id = id;
        this.name = name;
    }
    
    public int id(){
    	return id;
    }
    
    public String name(){
    	return name;
    }
}
