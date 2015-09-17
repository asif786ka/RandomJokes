package com.randomjokes.app.model;

public class JokesModel {

	private String jokeContent;

	public JokesModel() {

	}

	public JokesModel(String jokeContent) {
		super();
		this.jokeContent = jokeContent;
	}

	public String getJokeContent() {
		return jokeContent;
	}

	public void setJokeContent(String jokeContent) {
		this.jokeContent = jokeContent;
	}

}
