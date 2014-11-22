package org.echovantage.sample;

import java.util.function.Function;

import org.echovantage.sample.SampleMapObject.Values;
import org.echovantage.util.ObjectMap.MapEntries;

@MapEntries(Values.class)
public class SampleMapObject {
	public enum Values implements Function<SampleMapObject, Object> {
		id(SampleMapObject::getId),
		name(SampleMapObject::getName),
		description(SampleMapObject::getDescription);
		private final Function<SampleMapObject, Object> function;

		private Values(final Function<SampleMapObject, Object> function) {
			this.function = function;
		}

		@Override
		public Object apply(final SampleMapObject t) {
			return function.apply(t);
		}
	}

	private final String description;
	private final String name;
	private final int id;

	public SampleMapObject(final int id, final String name, final String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (description == null ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final SampleMapObject other = (SampleMapObject)obj;
		if(description == null) {
			if(other.description != null) {
				return false;
			}
		} else if(!description.equals(other.description)) {
			return false;
		}
		if(id != other.id) {
			return false;
		}
		if(name == null) {
			if(other.name != null) {
				return false;
			}
		} else if(!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SampleMapObject [description=" + description + ", name=" + name + ", id=" + id + "]";
	}
}
