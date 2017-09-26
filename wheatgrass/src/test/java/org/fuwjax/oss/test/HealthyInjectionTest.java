package org.fuwjax.oss.test;

import static org.fuwjax.oss.inject.Injector.newInjector;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.fuwjax.oss.inject.Config;
import org.fuwjax.oss.inject.Injector;
import org.fuwjax.oss.sample.WontonModule;
import org.fuwjax.oss.sample.WontonTestModule;
import org.fuwjax.oss.sample.WontonUser;
import org.junit.Test;

public class HealthyInjectionTest {
	@Test
	public void testWontonInjection() {
		Injector injector = newInjector("src/test/resources/wonton.yaml", WontonModule.class);
		WontonUser user = injector.get(WontonUser.class);
		assertThat(user.name, equalTo("example"));
		assertThat(user.source, equalTo(new InetSocketAddress("www.example.com", 80)));
		assertThat(user.target, equalTo(new InetSocketAddress("fuwjax.com", 80)));
	}

	{
		Config $config1 = new Config("src/test/resources/wonton.yaml");
		WontonModule $wontonModule2 = new WontonModule();
		WontonUser $wontonUser3; {
			String name = $config1.get("name", String.class);
			SocketAddress source; {
				Config $config4 = $config1.namespace("source");
				String hostname = $config4.get("hostname", String.class);
				int port = $config4.get("port", int.class);
				source = $wontonModule2.address(hostname, port);
			}
			SocketAddress target; {
				Config $config5 = $config1.namespace("target");
				String hostname = $config5.get("hostname", String.class);
				int port = $config5.get("port", int.class);
				target = $wontonModule2.address(hostname, port);
			}
			$wontonUser3 = new WontonUser(name, source, target);
		}
	}

	@Test
	public void testWontonTestInjection() {
		Injector injector = newInjector(WontonTestModule.class, "src/test/resources/wonton.yaml");
		WontonUser user = injector.get(WontonUser.class);
		assertThat(user.name, equalTo("test"));
		assertThat(user.source, equalTo(new InetSocketAddress("localhost", 80)));
		assertThat(user.target, equalTo(new InetSocketAddress("fuwjax.com", 80)));
	}

	{
		WontonTestModule $wontonTestModule1 = new WontonTestModule();
		Config $config2 = new Config("src/test/resources/wonton.yaml");
		WontonUser $wontonUser3; {
			String name = $wontonTestModule1.name;
			SocketAddress source; {
				Config $config4 = $config2.namespace("source");
				int port = $config4.get("port", int.class);
				source = $wontonTestModule1.source(port);
			}
			SocketAddress target; {
				Config $config5 = $config2.namespace("target");
				String hostname = $config5.get("hostname", String.class);
				int port = $config5.get("port", int.class);
				target = $wontonTestModule1.address(hostname, port);
			}
			$wontonUser3 = new WontonUser(name, source, target);
		}
	}
}
