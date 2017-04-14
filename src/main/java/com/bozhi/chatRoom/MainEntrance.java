package com.bozhi.chatRoom;

import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class MainEntrance {
	public static void main(String[] args) throws Exception {
		// create a connection and channel
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection conn = factory.newConnection();
		Channel channel = conn.createChannel();

		// set channel properties
		String roomName = "chatRoom";

		// start communication
		System.out
				.println("Welcome to "
						+ roomName
						+ " and now you need to pay attention to two things\nthe first:you need to have a nickname\nthe second:input \"quit\" to end of communcition\n*****************");
		System.out.print("please input your nickname:");
		Scanner scanner = new Scanner(System.in);
		String nickname = null;
		while (true) {
			nickname = scanner.nextLine();
			if (StringUtils.isBlank(nickname)) {
				System.out
						.print("you need to have a nickname,now please input your nickname:");
			} else {
				break;
			}
		}
		System.out.println("hello " + nickname + ", welcome you to join the "
				+ roomName + " and now you can speek freely");

		String interactMode = "topic";
		String permissionRule = "*.user";
		String queueName = nickname;
		channel.exchangeDeclare(roomName, interactMode);
		channel.queueDeclare(queueName, false, false, true, null);
		channel.queueBind(queueName, roomName, permissionRule);

		// get the user said
		String content = null;
		while (true) {
			content = scanner.nextLine();
			if (!StringUtils.isBlank(content)) {
				if ("quit".equals(content)) {
					System.out.println("success to login out");
					break;
				} else {
					channel.basicPublish(roomName, permissionRule, null,
							(nickname + " said " + content).getBytes());
				}
				Consumer consumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag,
							Envelope envelope, AMQP.BasicProperties properties,
							byte[] body) throws IOException {
						String message = new String(body, "UTF-8");
						System.out.println(message);   
					}
				};
				channel.basicConsume(queueName, false, consumer);
			}
		}

		// close stream
		scanner.close();
	}
}
