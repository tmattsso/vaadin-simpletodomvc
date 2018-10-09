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

	/**
	 * Mode -> filter function
	 */
	final static Map<Mode, Predicate<? super Todo>> modeMap = new HashMap<>();

	static {
		modeMap.put(Mode.ALL, todo -> true);
		modeMap.put(Mode.ACTIVE, todo -> !todo.isDone());
		modeMap.put(Mode.COMPLETED, todo -> todo.isDone());
	}

	/**
	 * Todo object storage
	 */
	private final List<Todo> todos = new LinkedList<>();
	
	/**
	 * Current active mode (filter)
	 */
	private Mode currentMode = Mode.ALL;
	
	/**
	 * Collection for all buttons for easy theming for the 'active' one
	 */
	private final Map<Mode, Button> modeButtons = new HashMap<>();

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
		todos.forEach(t -> t.setDone(done));
		refresh();
	}

	private Component buildFooter() {

		numItemsLabel = new Span();

		final Button toggleAll = new Button("All");
		final Button toggleActive = new Button("Active");
		final Button toggleCompleted = new Button("Completed");

		final HorizontalLayout buttonsLayout = new HorizontalLayout(toggleAll, toggleActive, toggleCompleted);
		modeButtons.put(Mode.ALL, toggleAll);
		modeButtons.put(Mode.ACTIVE, toggleActive);
		modeButtons.put(Mode.COMPLETED, toggleCompleted);
		modeButtons.values().forEach(b -> b.getElement().setAttribute("theme", "tertiary"));

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

		clearCompletedButton = new Button("Clear completed");
		clearCompletedButton.getElement().setAttribute("theme", "tertiary");
		clearCompletedButton.addClickListener(e -> clearCompleted());
		
		footerLayout = new HorizontalLayout();
		footerLayout.setWidth("100%");
		footerLayout.setClassName("footer");
		footerLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		footerLayout.add(numItemsLabel, buttonsLayout, clearCompletedButton);
		footerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

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

		// don't remove from DOM to preserve layout
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

		// don't remove from DOM to preserve layout
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
