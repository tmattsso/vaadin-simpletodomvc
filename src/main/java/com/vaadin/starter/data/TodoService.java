package com.vaadin.starter.data;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class acts as a dummy backend for the project.
 */
public class TodoService {

	private final List<Todo> todos = new LinkedList<>();

	/**
	 * @return all Todo objects
	 */
	public List<Todo> getTodos() {
		return todos;
	}

	/**
	 * Adds given Todo
	 * 
	 * @param todo
	 */
	public void addTodo(Todo todo) {
		if (todo != null) {
			todos.add(todo);
		}
	}

	/**
	 * @return the full size of the Todo list
	 */
	public int size() {
		return todos.size();
	}

	/**
	 * Removes given Todo from the list
	 * 
	 * @param t
	 */
	public void remove(Todo t) {
		todos.remove(t);
	}

	/**
	 * Removes all Todos from the list that match the given predicate
	 * 
	 * @param predicate
	 */
	public void removeIfFilter(Predicate<? super Todo> predicate) {
		if (predicate != null)
			todos.removeIf(predicate);
	}
}
