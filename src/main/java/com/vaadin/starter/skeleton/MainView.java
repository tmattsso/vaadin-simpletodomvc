package com.vaadin.starter.skeleton;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.starter.data.Todo;
import com.vaadin.starter.data.TodoService;

/**
 * The main view contains a button and a template element.
 */
@BodySize(height = "100vh", width = "100vw")
@HtmlImport("styles/shared-styles.html")
@Route("")
@Theme(Lumo.class)
public class MainView extends VerticalLayout {

	private static final long serialVersionUID = 6424995339801673339L;

	/**
	 * Todo object storage
	 */
	private final TodoService todoService = new TodoService();

	private TextField inputField;
	private Checkbox selectAllChecbox;
	private final VerticalLayout itemsLayout;

	private HorizontalLayout footerLayout;
	private Span numItemsLabel;
	private Button clearCompletedButton;

	public MainView() {

		setClassName("main-layout");
		setWidth("100%");
		setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		setSpacing(true);

		// Horizontal size constraint wrapper
		final Div horizontalWrapper = new Div();
		horizontalWrapper.setWidth("500px");

		final H1 header = new H1("todos");
		header.addClassName("header");
		horizontalWrapper.add(header);

		// App content area (white area) wrapper
		final Div contentWrapper = new Div();
		contentWrapper.setClassName("panel");
		horizontalWrapper.add(contentWrapper);

		contentWrapper.add(buildHeader());

		itemsLayout = new VerticalLayout();
		itemsLayout.setWidth("100%");
		itemsLayout.setClassName("mainsection");
		itemsLayout.setSpacing(false);
		contentWrapper.add(itemsLayout);

		contentWrapper.add(buildFooter());

		add(horizontalWrapper);

		refresh();

	}

	private Component buildHeader() {

		selectAllChecbox = new Checkbox();
		selectAllChecbox.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				markAllDone(e.getValue());
			}
		});

		inputField = new TextField();
		inputField.setPlaceholder("What needs to be done?");
		add(inputField);

		inputField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
		inputField.addValueChangeListener(e -> {
			if (e.isFromClient()) {
				addItem(e.getValue());
			}
		});

		final HorizontalLayout header = new HorizontalLayout(selectAllChecbox, inputField);
		header.setFlexGrow(1, inputField);
		header.setWidth("100%");
		header.setSpacing(true);
		header.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		header.setClassName("header");

		return header;
	}

	private void markAllDone(Boolean done) {
		todoService.getTodos().forEach(t -> t.setDone(done));
		refresh();
	}

	private Component buildFooter() {

		numItemsLabel = new Span();

		clearCompletedButton = new Button("Clear completed");
		clearCompletedButton.getElement().setAttribute("theme", "tertiary");
		clearCompletedButton.addClickListener(e -> clearCompleted());

		footerLayout = new HorizontalLayout();
		footerLayout.setWidth("100%");
		footerLayout.setClassName("footer");
		footerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		footerLayout.add(numItemsLabel, clearCompletedButton);
		footerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

		return footerLayout;
	}

	private void clearCompleted() {

		// clear CB
		selectAllChecbox.setValue(false);

		todoService.removeIfFilter(todo -> todo.isDone());
		refresh();
	}

	private void addItem(String value) {

		// clear CB
		selectAllChecbox.setValue(false);

		todoService.addTodo(new Todo(value));
		inputField.clear();
		refresh();
	}

	private void refresh() {

		// don't remove from DOM to preserve layout
		selectAllChecbox.getStyle().set("visibility", todoService.size() > 0 ? "visible" : "hidden");

		refreshItems();
		refreshFooter();
	}

	private void refreshFooter() {

		footerLayout.setVisible(todoService.size() != 0);

		final long count = todoService.getTodos().stream().filter(todo -> !todo.isDone()).count();
		numItemsLabel.setText(count + " items left");

		final long completedCount = todoService.getTodos().stream().filter(todo -> todo.isDone()).count();

		// don't remove from DOM to preserve layout
		clearCompletedButton.getStyle().set("visibility", completedCount > 0 ? "visible" : "hidden");
	}

	private void refreshItems() {
		itemsLayout.removeAll();

		itemsLayout.setVisible(todoService.size() != 0);

		todoService.getTodos().stream().forEach(t -> itemsLayout.add(new TodoLine(t, this::refresh, this::deleteTodo)));

	}

	void deleteTodo(Todo t) {
		todoService.remove(t);
		refresh();
	}
}
