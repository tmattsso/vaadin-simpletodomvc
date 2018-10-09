package com.vaadin.starter.skeleton;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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

/**
 * The main view contains a button and a template element.
 */
@BodySize(height = "100vh", width = "100vw")
@HtmlImport("styles/shared-styles.html")
@Route("")
@Theme(Lumo.class)
public class MainView extends VerticalLayout {

	private static final long serialVersionUID = 6424995339801673339L;

	private enum Mode {
		ALL, ACTIVE, COMPLETED
	};

	final static Map<Mode, Predicate<? super Todo>> modeMap = new HashMap<>();

	static {
		modeMap.put(Mode.ALL, todo -> true);
		modeMap.put(Mode.ACTIVE, todo -> !todo.isDone());
		modeMap.put(Mode.COMPLETED, todo -> todo.isDone());
	}

	private final List<Todo> todos = new LinkedList<>();
	private Mode currentMode = Mode.ALL;
	private final Map<Mode, Button> modeButtons = new HashMap<>();

	private Span numItemsLabel;
	private final VerticalLayout itemsLayout;
	private TextField inputField;
	private HorizontalLayout footerLayout;
	private Button clearCompletedButton;
	private Checkbox selectAllChecbox;

	public MainView() {
		setClassName("main-layout");

		setWidth("100%");
		setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		setSpacing(true);

		final VerticalLayout wrapper = new VerticalLayout();
		wrapper.setWidth("500px");
		wrapper.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

		final H1 header = new H1("todos");
		header.addClassName("header");
		wrapper.add(header);

		final Div panelWrapper = new Div();
		panelWrapper.setClassName("panel");
		wrapper.add(panelWrapper);

		panelWrapper.add(buildHeader());

		itemsLayout = new VerticalLayout();
		itemsLayout.setWidth("100%");
		itemsLayout.setClassName("mainsection");
		panelWrapper.add(itemsLayout);

		panelWrapper.add(buildFooter());

		add(wrapper);

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
		todos.forEach(t -> t.setDone(done));
		refresh();
	}

	private Component buildFooter() {
		footerLayout = new HorizontalLayout();
		footerLayout.setWidth("100%");
		footerLayout.setClassName("footer");
		footerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);

		numItemsLabel = new Span();

		final Button toggleAll = new Button("All");
		final Button toggleActive = new Button("Active");
		final Button toggleCompleted = new Button("Completed");

		final HorizontalLayout buttonsLayout = new HorizontalLayout(toggleAll, toggleActive, toggleCompleted);

		clearCompletedButton = new Button("Clear completed");

		footerLayout.add(numItemsLabel, buttonsLayout, clearCompletedButton);
		footerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

		modeButtons.put(Mode.ALL, toggleAll);
		modeButtons.put(Mode.ACTIVE, toggleActive);
		modeButtons.put(Mode.COMPLETED, toggleCompleted);

		modeButtons.values().forEach(b -> b.getElement().setAttribute("theme", "tertiary"));
		clearCompletedButton.getElement().setAttribute("theme", "tertiary");

		toggleAll.addClickListener(e -> {
			currentMode = Mode.ALL;
			refresh();
		});
		toggleActive.addClickListener(e -> {
			currentMode = Mode.ACTIVE;
			refresh();
		});
		toggleCompleted.addClickListener(e -> {
			currentMode = Mode.COMPLETED;
			refresh();
		});
		clearCompletedButton.addClickListener(e -> clearCompleted());

		return footerLayout;
	}

	private void clearCompleted() {

		// clear CB
		selectAllChecbox.setValue(false);

		todos.removeIf(modeMap.get(Mode.COMPLETED));
		refresh();
	}

	private void addItem(String value) {

		// clear CB
		selectAllChecbox.setValue(false);

		todos.add(new Todo(value));
		inputField.clear();
		refresh();
	}

	private void refresh() {

		// don't remve from dom
		selectAllChecbox.getStyle().set("visibility", todos.size() > 0 ? "visible" : "hidden");

		refreshItems();
		refreshFooter();
	}

	private void refreshFooter() {

		footerLayout.setVisible(todos.size() != 0);

		final long count = todos.stream().filter(modeMap.get(Mode.ACTIVE)).count();
		numItemsLabel.setText(count + " items left");

		modeButtons.values().forEach(c -> c.removeClassName("selected"));
		modeButtons.get(currentMode).addClassName("selected");

		final long completedCount = todos.stream().filter(modeMap.get(Mode.COMPLETED)).count();

		// don't remve from dom
		clearCompletedButton.getStyle().set("visibility", completedCount > 0 ? "visible" : "hidden");
	}

	private void refreshItems() {
		itemsLayout.removeAll();

		itemsLayout.setVisible(todos.size() != 0);

		todos.stream().filter(modeMap.get(currentMode))
				.forEach(t -> itemsLayout.add(new TodoLine(t, this::refresh, this::deleteTodo)));

	}

	void deleteTodo(Todo t) {
		todos.remove(t);
		refresh();
	}
}
