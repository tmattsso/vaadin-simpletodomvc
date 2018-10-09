package com.vaadin.starter.data;

public class Todo {

	private String name;
	private boolean done;

	public Todo() {

	}

	public Todo(String name) {
		this.name = name;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
