/*******************************************************************************
 * Copyright (c) 2014 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz
 *******************************************************************************/
package org.eclipse.ice.reactor.plant.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.ice.datastructures.componentVisitor.IReactorComponent;
import org.eclipse.ice.datastructures.componentVisitor.SelectiveComponentVisitor;
import org.eclipse.ice.datastructures.updateableComposite.Component;
import org.eclipse.ice.reactor.plant.HeatExchanger;
import org.eclipse.ice.reactor.plant.Pipe;
import org.eclipse.ice.reactor.plant.PlantComponent;
import org.eclipse.ice.reactor.plant.SelectivePlantComponentVisitor;
import org.eclipse.ice.reactor.plant.VolumeBranch;
import org.junit.Test;

/**
 * <!-- begin-UML-doc --> <!-- end-UML-doc -->
 * 
 * @author w5q
 * @generated 
 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
public class VolumeBranchTester {
	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Boolean flag to mark if the PlantComponent was successfully visited.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	private boolean wasVisited = false;

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Checks the construction of the component.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@Test
	public void checkConstruction() {
		// begin-user-code
		VolumeBranch branch = new VolumeBranch();
		assertNotNull(branch.getInputs());
		assertNotNull(branch.getOutputs());
		assertTrue(branch.getInputs().isEmpty());
		assertTrue(branch.getOutputs().isEmpty());
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Checks the getting and setting of the component's attributes.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@Test
	public void checkProperties() {
		// begin-user-code
		// Create some inputs and outputs
		PlantComponent input1 = new Pipe();
		input1.setName("input1");
		PlantComponent input2 = new Pipe();
		input2.setName("input2");
		PlantComponent output1 = new Pipe();
		output1.setName("output1");
		PlantComponent output2 = new HeatExchanger();
		output2.setName("output2");
		ArrayList<PlantComponent> inputs = new ArrayList<PlantComponent>();
		ArrayList<PlantComponent> outputs = new ArrayList<PlantComponent>();
		inputs.add(input1);
		inputs.add(input2);
		outputs.add(output1);
		outputs.add(output2);

		// Create a Branch with inputs and outputs, note we are
		// ignoring the connections as it's unclear how they should work
		VolumeBranch branch = new VolumeBranch();
		assertTrue(branch.getInputs().isEmpty());
		assertTrue(branch.getOutputs().isEmpty());

		// Set the new input and outputs
		branch.setInputs(inputs);
		branch.setOutputs(outputs);

		// Assert that the lists were set correctly
		assertTrue(branch.getInputs().equals(inputs));
		assertTrue(branch.getOutputs().equals(outputs));

		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Checks the hashCode and equality methods of the component.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@Test
	public void checkEquality() {
		// begin-user-code
		// Create two equal VolumeBranches with
		// nullary constructor
		VolumeBranch b = new VolumeBranch();
		b.setName("b");
		b.setDescription("Description");

		VolumeBranch equalB = new VolumeBranch();
		equalB.setName("b");
		equalB.setDescription("Description");

		// Check reflexivity
		assertTrue(b.equals(b));

		// Check equality and symmetry
		assertTrue(b.equals(equalB));
		assertTrue(equalB.equals(b));

		// Since equal, hashcodes should be equal
		assertTrue(b.hashCode() == equalB.hashCode());
		assertTrue(b.hashCode() == b.hashCode());

		// Make them unequal
		b.setName("b_newName");

		// Check inequality and symmetry
		assertFalse(b.equals(equalB));
		assertFalse(equalB.equals(b));

		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Checks the copy and clone methods of the component.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@Test
	public void checkCopying() {
		// begin-user-code
		// Create a VolumeBranch to copy
		VolumeBranch branch = new VolumeBranch();
		branch.setName("Name");
		branch.setDescription("desc");
		PlantComponent input1 = new Pipe();
		input1.setName("input1");
		PlantComponent input2 = new Pipe();
		input2.setName("input2");
		PlantComponent output1 = new Pipe();
		output1.setName("output1");
		PlantComponent output2 = new HeatExchanger();
		output2.setName("output2");
		ArrayList<PlantComponent> inputs = new ArrayList<PlantComponent>();
		ArrayList<PlantComponent> outputs = new ArrayList<PlantComponent>();
		inputs.add(input1);
		inputs.add(input2);
		outputs.add(output1);
		outputs.add(output2);
		branch.setInputs(inputs);
		branch.setOutputs(outputs);

		// Create a copy Branch
		VolumeBranch copy = new VolumeBranch();

		// Assert they are not the same to begin with
		assertFalse(copy.equals(branch));

		// Copy Branch
		copy.copy(branch);

		// Check the copy succeeded
		assertTrue("Name".equals(copy.getName()));
		assertTrue("desc".equals(copy.getDescription()));
		assertFalse(copy == branch);
		assertTrue(copy.getInputs().equals(inputs));
		assertTrue(copy.getOutputs().equals(outputs));

		// Get a clone to verify the clone() method
		Object clone = branch.clone();

		assertNotNull(clone);
		assertFalse(clone == branch);
		assertTrue(clone.equals(branch));

		assertTrue(clone instanceof VolumeBranch);

		VolumeBranch casted = (VolumeBranch) clone;

		assertFalse(casted == branch);
		assertTrue(casted.equals(branch));

		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Checks for persistence in the component.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public void checkPersistence() {
		// begin-user-code
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Checks the visitation routine of the component.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@Test
	public void checkVisitation() {
		// begin-user-code

		// Create a new component to visit.
		VolumeBranch component = new VolumeBranch();

		// Create an invalid visitor, and try to visit the component.
		FakeComponentVisitor visitor = null;
		component.accept(visitor);

		// Check that the component wasn't visited yet.
		assertFalse(wasVisited);

		// Create a valid visitor, and try to visit the component.
		visitor = new FakeComponentVisitor();
		component.accept(visitor);

		// Check that the component was visited.
		assertTrue(wasVisited);

		// Grab the visitor's visited component.
		Component visitorComponent = visitor.component;

		// Check that the visitor's component is the same component we initially
		// created.
		assertTrue(component == visitorComponent);
		assertTrue(component.equals(visitorComponent));

		// ---- Check PlantComponent visitation. ---- //
		wasVisited = false;

		// Create an invalid visitor, and try to visit the component.
		FakePlantComponentVisitor plantVisitor = null;
		component.accept(plantVisitor);

		// Check that the component wasn't visited yet.
		assertFalse(wasVisited);

		// Create a valid visitor, and try to visit the component.
		plantVisitor = new FakePlantComponentVisitor();
		component.accept(plantVisitor);

		// Check that the component was visited.
		assertTrue(wasVisited);

		// Grab the visitor's visited component.
		PlantComponent visitorPlantComponent = plantVisitor.component;

		// Check that the visitor's component is the same component we initially
		// created.
		assertTrue(component == visitorPlantComponent);
		assertTrue(component.equals(visitorPlantComponent));

		return;
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Fake class to test the visitation routine of the component.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @author w5q
	 */
	private class FakeComponentVisitor extends SelectiveComponentVisitor {

		// The fake visitor's visited component.
		private IReactorComponent component = null;

		@Override
		public void visit(IReactorComponent component) {

			// Set the IComponentVisitor component (if valid), and flag the
			// component as having been visited.
			if (component != null) {
				this.component = component;
				wasVisited = true;
			}
			return;
		}
	};

	/**
	 * Fake class to test the PlantComponent visitation routine.
	 * 
	 * @author Jordan
	 * 
	 */
	private class FakePlantComponentVisitor extends
			SelectivePlantComponentVisitor {

		// The fake visitor's visited component.
		private PlantComponent component = null;

		@Override
		public void visit(VolumeBranch plantComp) {
			// Set the IComponentVisitor component (if valid), and flag the
			// component as having been visited.
			if (plantComp != null) {
				this.component = plantComp;
				wasVisited = true;
			}
			return;
		}
	}
}