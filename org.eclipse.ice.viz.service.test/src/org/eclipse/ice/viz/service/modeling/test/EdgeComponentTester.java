/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.modeling.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.ice.viz.service.modeling.AbstractController;
import org.eclipse.ice.viz.service.modeling.AbstractView;
import org.eclipse.ice.viz.service.modeling.EdgeComponent;
import org.eclipse.ice.viz.service.modeling.Vertex;
import org.eclipse.ice.viz.service.modeling.VertexComponent;
import org.junit.Test;

/**
 * A class for testing Edge's functionality
 * 
 * @author Robert Smith
 *
 */
public class EdgeComponentTester {

	/**
	 * Checks that the edge correctly manages its vertices
	 */
	@Test
	public void checkVertices() {

		// Create the edge
		EdgeComponent edge = new EdgeComponent();

		// The edge should initially have length 0
		assertEquals(0, Double.compare(edge.getLength(), 0d));

		// Try adding a non-vertex. It should be put in the Default category
		edge.addEntity(new AbstractController());
		assertEquals(1, edge.getEntitiesByCategory("Default").size());

		// Create some vertices
		Vertex vertex1 = new Vertex(new VertexComponent(0, 0, 0),
				new AbstractView());
		Vertex vertex2 = new Vertex(new VertexComponent(1, 1, 1),
				new AbstractView());
		Vertex vertex3 = new Vertex(new VertexComponent(2, 2, 2),
				new AbstractView());

		// Add all three vertices to the edge.
		edge.addEntity(vertex1);
		edge.addEntity(vertex2);
		edge.addEntity(vertex3);

		// Check the Vertices category to ensure that the edge accepted the
		// first two vertices and ignored the third
		List<AbstractController> vertices = edge
				.getEntitiesByCategory("Vertices");
		assertTrue(vertices.contains(vertex1));
		assertTrue(vertices.contains(vertex2));
		assertFalse(vertices.contains(vertex3));
		assertEquals(2, vertices.size());

		// Replace the second vertex with the third
		edge.removeEntity(vertex2);
		edge.addEntity(vertex3);

		// Check the Vertices category to ensure that the last vertex was
		// replaced
		vertices = edge.getEntitiesByCategory("Vertices");
		assertTrue(vertices.contains(vertex1));
		assertFalse(vertices.contains(vertex2));
		assertTrue(vertices.contains(vertex3));
	}
}
