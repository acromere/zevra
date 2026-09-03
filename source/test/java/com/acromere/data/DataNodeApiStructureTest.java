package com.acromere.data;

import com.acromere.transaction.TxnEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataNodeApiStructureTest extends BaseDataNodeTest {

	@Test
	void testGetParent() {
		MockDataNode parent = new MockDataNode();
		MockDataNode child = new MockDataNode();
		assertThat( child.<DataNode> getParent() ).isNull();

		String key = "key";

		parent.setValue( key, child );
		assertThat( child.<MockDataNode> getParent() ).isEqualTo( parent );

		parent.setValue( key, null );
		assertThat( child.<DataNode> getParent() ).isNull();
	}

	@Test
	void testGetNodePath() {
		MockDataNode parent = new MockDataNode();
		MockDataNode child = new MockDataNode();

		parent.setValue( "child", child );

		List<DataNode> path = parent.getNodePath();
		assertThat( path.size() ).isEqualTo( 1 );
		assertThat( path.get( 0 ) ).isEqualTo( parent );

		path = child.getNodePath();
		assertThat( path.size() ).isEqualTo( 2 );
		assertThat( path.get( 0 ) ).isEqualTo( parent );
		assertThat( path.get( 1 ) ).isEqualTo( child );
	}

	@Test
	void testParentGetsModifiedAndUnmodifiedWithChildModifyFlag() {
		MockDataNode grandparent = new MockDataNode( "grandparent" );
		MockDataNode parent = new MockDataNode( "parent" );
		grandparent.setValue( "child", parent );
		grandparent.setModified( false );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );

		assertThat( grandparent.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		NodeAssert.assertThat( grandparent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );

		child.setModified( true );
		assertThat( grandparent.isModified() ).isTrue();
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		NodeAssert.assertThat( grandparent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, true, 0, 0 );

		parent.setModified( false );
		assertThat( grandparent.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		NodeAssert.assertThat( grandparent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
	}

	@Test
	void testParentGetsModifiedEventsWhenChildModifiedAndUnmodified() {
		// Start with a standard parent/child model
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "key", "value0" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Change the attribute value on the child
		child.setValue( "key", "value1" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
		// The parent is already modified so there should not be a modified event here
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Set the child attribute back to null to unmodify the child and parent
		child.setValue( "key", null );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", "value1", null );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testChildModifiedClearedWhenParentModifiedCleared() {
		// Start with a standard parent/child model
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "key", "value0" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Clear the parent modified flag
		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );

		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNullStartValue() {
		// Start with a standard parent/child model
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Test setting a value on the child node modifies the parent
		child.setValue( "key", "value" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Test clear the value on the child node unmodifies the parent
		child.setValue( "key", null );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNonNullStartValue() {
		// Start with a standard parent/child model
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Set an attribute on the child to a non-null value
		child.setValue( "key", "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );

		// Clear the modified flags
		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_END );

		// Change the attribute value on the child
		child.setValue( "key", "value1" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Set the child attribute to the same value, should do nothing
		child.setValue( "key", "value1" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Set the child attribute back to value0
		child.setValue( "key", "value0" );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "key", "value1", "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testGrandparentModifiedByChildNodeAttributeChange() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		MockDataNode grandChild = new MockDataNode( "grandChild" );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		parent.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.ADDED );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		child.setValue( "child", grandChild );
		assertThat( grandChild.<DataNode> getParent() ).isEqualTo( child );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.CHILD_ADDED, "child", null, grandChild );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "child", null, grandChild );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, grandChild );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, grandChild );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.ADDED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		parent.setModified( false );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		// Test setting a value on the child node modifies the parents
		grandChild.setValue( "key", "value" );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( grandChild ).hasStates( true, false, 1, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, DataNodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( parent, DataNodeEvent.MODIFIED, null, null, null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, DataNodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child, DataNodeEvent.MODIFIED, null, null, null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		// Test unsetting the value on the child node unmodifies the parents
		grandChild.setValue( "key", null );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, DataNodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, DataNodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );
	}

	@Test
	void testParentModifiedByChildNodeClearedByFlag() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.ADDED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setModified( false );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "1" );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "a", null, "1" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "a", null, "1" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "1" );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 2, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "b", null, "1" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "b", null, "1" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Set this state as the new unmodified state
		child.setModified( false );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testParentModifiedByChildNodeClearedByValue() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.ADDED );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "2" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "a", null, "2" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "a", null, "2" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "2" );
		NodeAssert.assertThat( child ).hasStates( true, false, 2, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "b", null, "2" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "b", null, "2" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test unsetting the 'a' value on the child leaves the parent modified
		child.setValue( "a", null );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "a", "2", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "a", "2", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test unsetting the value 'b' on the child returns the parent to unmodified
		child.setValue( "b", null );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "b", "2", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "b", "2", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testChildModifiedClearedByParentSetModifiedFalse() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.ADDED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "x", "2" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, DataNodeEvent.VALUE_CHANGED, "x", null, "2" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "x", null, "2" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setModified( false );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testAddNodeAttributeToDifferentParent() {
		MockDataNode parent0 = new MockDataNode( "parent0" );
		MockDataNode parent1 = new MockDataNode( "parent1" );
		MockDataNode child = new MockDataNode( "child" );
		int parent0Index = 0;
		int parent1Index = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent0 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Add the child attribute to parent 0
		parent0.setValue( "child", child );
		NodeAssert.assertThat( parent0 ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.ADDED );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent0, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );

		// Clear the modified flag of parent 0.
		parent0.setModified( false );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent0 );
		NodeAssert.assertThat( parent0 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Add the child attribute to parent 1.
		parent1.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent1 );
		assertThat( parent1.<DataNode> getValue( "child" ) ).isEqualTo( child );
		assertThat( parent0.<DataNode> getValue( "child" ) ).isNull();
		NodeAssert.assertThat( parent0 ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.REMOVED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.CHILD_REMOVED, "child", child, null );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", child, null );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_END );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.ADDED );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( DataNodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( DataNodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( DataNodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( TxnEvent.COMMIT_END );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent1, NodeEvent.PARENT_CHANGED );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testChildReceivesParentChangedEvent() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( child.getEventCount() ).isEqualTo( childIndex += 1 );

		parent.addModifyingKeys( "x" );
		parent.setValue( "x", 1 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, DataNodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testGrandchildReceivesParentChangedEvent() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		MockDataNode grandChild = new MockDataNode( "grandChild" );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		parent.setValue( "child", child );
		assertThat( child.<DataNode> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( child.getEventCount() ).isEqualTo( childIndex += 1 );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		child.setValue( "child", grandChild );
		assertThat( grandChild.<DataNode> getParent() ).isEqualTo( child );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( child.getEventCount() ).isEqualTo( childIndex += 7 );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex += 1 );

		parent.addModifyingKeys( "x" );
		parent.setValue( "x", 1 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, DataNodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( parent, DataNodeEvent.PARENT_CHANGED );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );
	}

	@Test
	void testParentNodeNotModifiedByNodeAddedWithSetWithModifyFilter() {
		int parentIndex = 12;
		int childIndex = 1;

		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.setSetModifyFilter( MockDataNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( child.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		MockDataNode item0 = new MockDataNode( "0" );
		item0.setValue( "dont-modify", true );
		assertThat( item0.isModified() ).isFalse();
		// No new events should have occurred
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		// No new events should have occurred
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.addItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.CHILD_ADDED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.CHILD_ADDED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		child.addItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.CHILD_REMOVED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.CHILD_REMOVED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), DataNodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( DataNodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockDataNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testNodeNotModifiedByChildAddUntilNodeFilterValueChange() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.setSetModifyFilter( MockDataNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();

		MockDataNode item0 = new MockDataNode( "A" );

		// Make sure the modified flag is working as expected before using the dont-modify value
		child.addItem( item0 );
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		assertThat( item0.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// The dont-modify is not a modifying key
		item0.setValue( "dont-modify", true );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// Adding the child should not cause any node to be modified
		child.addItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// Some extra checking
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();
		child.addItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();
	}

	@Test
	void testNodeNotModifiedByChildRemoveUntilNodeFilterValueChange() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.setSetModifyFilter( MockDataNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();

		MockDataNode item0 = new MockDataNode( "A" );

		// Make sure the modified flag is working as expected before using the dont-modify value
		child.addItem( item0 );
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		assertThat( item0.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		item0.setValue( "dont-modify", true );
		child.addItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// This does not modify the node because dont-modify is not a modifying key
		item0.setValue( "dont-modify", null );
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		assertThat( item0.isModified() ).isFalse();
	}

	@Test
	void testParentNodeNotModifiedByNodeSetWithModifyFilter() {
		MockDataNode parent = new MockDataNode( "parent" );
		MockDataNode child = new MockDataNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );

		child.setSetModifyFilter( MockDataNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( child.isModified() ).isFalse();

		MockDataNode item0 = new MockDataNode( "0" );
		child.addItem( item0 );
		assertThat( item0.isModified() ).isFalse();
		assertThat( child.isModified() ).isTrue();
		assertThat( parent.isModified() ).isTrue();
		child.setModified( false );

		assertThat( item0.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModifiedByValue() ).isFalse();
		assertThat( parent.isModifiedByChild() ).isFalse();
		assertThat( parent.isModified() ).isFalse();

		item0.setValue( "dont-modify", true );
		assertThat( item0.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();

		item0.setValue( "a", "A" );
		assertThat( item0.isModified() ).isTrue();
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();

		item0.setValue( "dont-modify", null );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( child.isModified() ).isTrue();
		assertThat( parent.isModified() ).isTrue();
		child.setModified( false );
	}

	@Test
	void testParentModifiedByAddingModifiedChild() {
		MockDataNode child = new MockDataNode( "child" );
		child.setModified( true );
		assertThat( data.isModified() ).isFalse();
		assertThat( child.isModified() ).isTrue();

		data.addItem( new MockDataNode().addItem( new MockDataNode().addItem( child ) ) );
		assertThat( data.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();

		data.setModified( false );
		assertThat( data.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
	}

}
