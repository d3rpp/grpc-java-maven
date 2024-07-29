package com.example.routeguide;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.routeguide.Feature;
import io.grpc.examples.routeguide.Point;
import io.grpc.examples.routeguide.RouteGuideGrpc;
import io.grpc.examples.routeguide.RouteGuideGrpc.RouteGuideBlockingStub;
import io.grpc.examples.routeguide.RouteGuideGrpc.RouteGuideStub;

public class RouteGuideClient {
	public static final Logger logger = Logger.getLogger(RouteGuideClient.class.getName());

	private final RouteGuideBlockingStub blockingStub;
	private final RouteGuideStub asyncStub;

	private Random random = new Random();
	
	/** Construct client for accessing RouteGuideServer using the existing channel. */
	public RouteGuideClient(Channel channel) {
		blockingStub = RouteGuideGrpc.newBlockingStub(channel);
		asyncStub = RouteGuideGrpc.newStub(channel);
	}

	public void getFeature(int lat, int lon) {
		info("*** GetFeature: lat={0} lon={1}", lat, lon);

		Point request = Point.newBuilder().setLatitude(lat).setLongitude(lon).build();

		Feature feature;
		try {
			feature = blockingStub.getFeature(request);
		} catch (StatusRuntimeException e) {
			warning("RPC Failed: {0}", e);
			return;
		}

		if (RouteGuideUtil.exists(feature)) {
			info(
					"Found Feature called \"{0}\" at {1}, {2}",
					feature.getName(),
					RouteGuideUtil.getLatitude(feature.getLocation()),
					RouteGuideUtil.getLongitude(feature.getLocation()));
		} else {
			info("No Feature Found at {0}, {1}", lat, lon);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		String target = "127.0.0.1:8980";

		if (args.length > 0) {
			if ("--help".equals(args[0])) {
				System.err.println("Shut up");
				System.exit(1);
			}

			target = args[0];
		}
		
		ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
				.build();

		try {
			RouteGuideClient client = new RouteGuideClient(channel);
			client.getFeature(409146138, -746188906);
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private void info(String msg, Object... params) {
		logger.log(Level.INFO, msg, params);
	}

	private void warning(String msg, Object... params) {
		logger.log(Level.WARNING, msg, params);
	}
}
