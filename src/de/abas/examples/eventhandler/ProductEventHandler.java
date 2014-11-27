package de.abas.examples.eventhandler;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import de.abas.eks.jfop.FOPExitException;
import de.abas.erp.api.AppContext;
import de.abas.erp.api.commands.CommandFactory;
import de.abas.erp.api.commands.FieldManipulator;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.common.type.enums.EnumEditorAction;
import de.abas.erp.db.field.editable.EditableFieldMeta;
import de.abas.erp.db.infosystem.standard.la.PlanChart;
import de.abas.erp.db.infosystem.standard.st.StructuralBOMTreeView;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;

/**
 * The ProductEventHandler handles events occurring in a product object (database 2:1).
 * This class shows the use of a field's meta data and how to open an infosystem with parameters using AJO.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
@EventHandler(head = ProductEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class ProductEventHandler {

	/**
	 * Outputs the product description in different languages, outputs the procurement period in working days and outputs the vendors.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, ProductEditor head) throws EventException {
		// declares local variables
		String value = "";
		EditableFieldMeta fieldMeta = null;

		// outputs product description in different languages
		// gets field values by using the meta data of the field to be able to iterate in a loop
		ctx.out().println("Bezeichnung intern in verschiedenen Sprachen ausgeben");
		for (int i = 0; i < 5; i++) {
			if (i != 0) {
				// iteration step 2 to n
				fieldMeta = head.getFieldMeta("descr" + i);
			}
			else {
				// iteration step 1
				fieldMeta = head.getFieldMeta("descr");
			}
			value = fieldMeta.getValue();
			ctx.out().println(i + " -> " + value);
		}

		// outputs procurement periods per vendor in working days
		ctx.out().println("Beschaffgungsfrist in Arbeitstagen ausgeben");
		ctx.out().println("Lieferanten ausgeben");
		value = "";
		for (int i = 1; i < 5; i++) {
			if (i != 1) {
				fieldMeta = head.getFieldMeta("procurePeriodWorkDay" + i);
			}
			else {
				fieldMeta = head.getFieldMeta("procurePeriodWorkDay");
			}
			value = fieldMeta.getValue();
			ctx.out().println(i + " -> " + value);
		}

		// outputs vendors
		ctx.out().println("Lieferanten ausgeben");
		value = "";
		for (int i = 1; i < 5; i++) {
			if (i != 1) {
				fieldMeta = head.getFieldMeta("vendor" + i);
			}
			else {
				fieldMeta = head.getFieldMeta("vendor");
			}
			value = fieldMeta.getValue();
			ctx.out().println(i + " -> " + value);
		}
	}

	/**
	 * Change history for field drawingNorm. If field drawingNorm is updated, the date and new value of the field is stored in freeText2.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ScreenEventHandler(type = ScreenEventType.VALIDATION)
	public void screenValidation(ScreenEvent event, ScreenControl screenControl, DbContext ctx, ProductEditor head) throws EventException {
		// gets screen mode that triggered the event
		EnumEditorAction screenMode = event.getCommand();
		// continues only if event was triggered in edit mode
		if (screenMode.equals(EnumEditorAction.Edit)) {
			// checks whether drawingNorm was modified
			boolean isModified = ProductEditor.META.drawingNorm.isModified(head);
			if (isModified) {
				// gets current date from client
				AbasDate abasDate = new AbasDate();
				// checks whether freeText2 is empty
				boolean isEmptyFreeText2 = ProductEditor.META.freeText2.isEmpty(head);
				// if freeText2 is empty it is overwritten
				if (isEmptyFreeText2) {
					try {
						// stores date and content of field drawingNorm
						String write = abasDate + " -> " + head.getDrawingNorm();
						// uses StringReader object to read the String variable write and writes its content to freeText2
						head.setFreeText2(new StringReader(write));
					}
					catch (IOException e) {
						throw new FOPExitException("Cannot write freetext2", 1);
					}
				}
				// else freeText2 already has content, so new content is appended
				else {
					try {
						// uses StringWriter object to read current content of freeText2
						Writer writer = head.getFreeText2(new StringWriter());
						// gets new content of field drawingNorm
						String write = head.getDrawingNorm();
						// appends line break
						writer.append('\n');
						// appends current date to String variable write
						write = abasDate + " -> " + write;
						// appends content of variable write to Writer object
						writer.append(write);
						// uses StringReader to Read content of Writer object and assigns it to freeText2
						head.setFreeText2(new StringReader(writer.toString()));

					}
					catch (IOException e) {
						throw new FOPExitException("Cannot append to freeText2", 1);
					}
				}
			}
			else {
				ctx.out().println("Field drawingNorm was not changed.");
			}
		}
	}

	/**
	 * This class handles the ButtonAfter logic of the custom button yisplanchart.
	 * 
	 * Variable table row of the button yisplankarte's definition:
	 * 2  14 xxyisplanchart  -                      BU3          -  2  1 0 1 0 1 6 0 A A 0 #plan chart # #
	 * 
	 * The button yisplanchart opens the infosystem PlanChart using parameters.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field="yisplanchart", type = ButtonEventType.AFTER)
	public void yisplanchartAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx,ProductEditor head) throws EventException {
		ctx.out().println("call infosystem ");
		
		// creates a CommandFactory object
		CommandFactory commandFactory = AppContext.createFor(ctx).getCommandFactory();
		// create a FieldManipulator object of PlanChart as parameter for the infosystem PlanChart 
		FieldManipulator<PlanChart> scrParamBuilder = commandFactory.getScrParamBuilder(PlanChart.class);
		
		// adds product as parameter and presses start button
		scrParamBuilder.setReference(PlanChart.META.kart, head);
		scrParamBuilder.pressButton(PlanChart.META.start);
		
		// opens the infosystem PlanChart using the previously defined parameters
		commandFactory.startInfosystem(PlanChart.class, scrParamBuilder);
	}
	
	
	/**
	 * This class handles the ButtonAfter logic of the custom button yissubbom.
	 * 
	 * Variable table row of the button ycallis' definition:
	 * 2  15 xxyissubbom     -                      BU3          -  2  1 0 1 0 1 6 0 A A 0 #sub BOM # #
	 * 
	 * The button yissubbom opens the infosystem StructuralBOMTreeView using parameters.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The ProductEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field="yissubbom", type = ButtonEventType.AFTER)
	public void yissubbomAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx,ProductEditor head) throws EventException {
		// creates a CommandFactory object
		CommandFactory commandFactory = AppContext.createFor(ctx).getCommandFactory();
		// creates a FieldManipulator object of StructuralBOMTreeView as parameter for the infosystem StructuralBOMTreeView
		FieldManipulator<StructuralBOMTreeView> scrParamBuilder = commandFactory.getScrParamBuilder(StructuralBOMTreeView.class);
		
		// adds the product to the parameter for the infosystem StructuralBOMTreeView
		scrParamBuilder.setReference(StructuralBOMTreeView.META.artikel, head);
		// adds activated start button to the parameter for the infosystem StructuralBOMTreeView
		scrParamBuilder.pressButton(StructuralBOMTreeView.META.start);
		
		// opens the infosystem StructuralBOMTreeView using the previously defined parameters
		commandFactory.startInfosystem(StructuralBOMTreeView.class, scrParamBuilder);
	}

}
