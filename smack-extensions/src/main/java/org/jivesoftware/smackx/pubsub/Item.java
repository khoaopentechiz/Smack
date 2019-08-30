/**
 *
 * Copyright the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.XmlStringBuilder;

import org.jivesoftware.smackx.pubsub.provider.ItemProvider;

/**
 * This class represents an item that has been, or will be published to a
 * PubSub node.  An <code>Item</code> has several properties that are dependent
 * on the configuration of the node to which it has been or will be published.
 *
 * <h3>An Item received from a node (via {@link LeafNode#getItems()} or {@link LeafNode#addItemEventListener(org.jivesoftware.smackx.pubsub.listener.ItemEventListener)}</h3>
 * <ul>
 * <li>Will always have an id (either user or server generated) unless node configuration has both
 * {@link ConfigureForm#isPersistItems()} and {@link ConfigureForm#isDeliverPayloads()}set to false.
 * <li>Will have a payload if the node configuration has {@link ConfigureForm#isDeliverPayloads()} set
 * to true, otherwise it will be null.
 * </ul>
 *
 * <h3>An Item created to send to a node (via {@link LeafNode#publish()} or {@link LeafNode#publish()}</h3>
 * <ul>
 * <li>The id is optional, since the server will generate one if necessary, but should be used if it is
 * meaningful in the context of the node.  This value must be unique within the node that it is sent to, since
 * resending an item with the same id will overwrite the one that already exists if the items are persisted.
 * <li>Will require payload if the node configuration has {@link ConfigureForm#isDeliverPayloads()} set
 * to true.
 * </ul>
 *
 * <p>
 * To customise the payload object being returned from the {@link PayloadItem#getPayload()} method, you can
 * add a custom parser as explained in {@link ItemProvider}.
 * </p>
 *
 * @author Robin Collier
 */
public class Item extends NodeExtension {
    public enum ItemNamespace {
        pubsub(PubSubElementType.ITEM),
        event(PubSubElementType.ITEM_EVENT),
        ;
        private final PubSubElementType type;

        ItemNamespace(PubSubElementType type) {
            this.type = type;
        }

        public static ItemNamespace fromXmlns(String xmlns) {
            for (ItemNamespace itemNamespace : ItemNamespace.values()) {
                if (itemNamespace.type.getNamespace().getXmlns().equals(xmlns)) {
                    return itemNamespace;
                }
            }
            throw new IllegalArgumentException("Invalid item namespace: " + xmlns);
        }
    }

    private final String itemId;

    /**
     * Create an empty <code>Item</code> with no id.  This is a valid item for nodes which are configured
     * so that {@link ConfigureForm#isDeliverPayloads()} is false.  In most cases an id will be generated by the server.
     * For nodes configured with {@link ConfigureForm#isDeliverPayloads()} and {@link ConfigureForm#isPersistItems()}
     * set to false, no <code>Item</code> is sent to the node, you have to use the {@link LeafNode#publish()}
     * method in this case.
     */
    public Item() {
        this(ItemNamespace.pubsub, null, null);
    }

    /**
     * Create an <code>Item</code> with an id but no payload.  This is a valid item for nodes which are configured
     * so that {@link ConfigureForm#isDeliverPayloads()} is false.
     *
     * @param itemId The id if the item.  It must be unique within the node unless overwriting and existing item.
     * Passing null is the equivalent of calling {@link #Item()}.
     */
    public Item(String itemId) {
        this(ItemNamespace.pubsub, itemId, null);
    }

    /**
     * Create an <code>Item</code> with an id but no payload.  This is a valid item for nodes which are configured
     * so that {@link ConfigureForm#isDeliverPayloads()} is false.
     *
     * @param itemNamespace the namespace of the item.
     * @param itemId The id if the item.  It must be unique within the node unless overwriting and existing item.
     * Passing null is the equivalent of calling {@link #Item()}.
     */
    public Item(ItemNamespace itemNamespace, String itemId) {
        this(itemNamespace, itemId, null);
    }

    /**
     * Create an <code>Item</code> with an id and a node id.
     * <p>
     * <b>Note:</b> This is not valid for publishing an item to a node, only receiving from
     * one as part of {@link Message}.  If used to create an Item to publish
     * (via {@link LeafNode#publish(Item)}, the server <i>may</i> return an
     * error for an invalid packet.
     *
     * @param itemId The id of the item.
     * @param nodeId The id of the node which the item was published to.
     */
    public Item(String itemId, String nodeId) {
        this(ItemNamespace.pubsub, itemId, nodeId);
    }

    /**
     * Create an <code>Item</code> with an id and a node id.
     * <p>
     * <b>Note:</b> This is not valid for publishing an item to a node, only receiving from
     * one as part of {@link Message}.  If used to create an Item to publish
     * (via {@link LeafNode#publish(Item)}, the server <i>may</i> return an
     * error for an invalid packet.
     *
     * @param itemNamespace the namespace of the item.
     * @param itemId The id of the item.
     * @param nodeId The id of the node which the item was published to.
     */
    public Item(ItemNamespace itemNamespace, String itemId, String nodeId) {
        super(itemNamespace.type, nodeId);
        this.itemId = itemId;
    }

    /**
     * Get the item id.  Unique to the node it is associated with.
     *
     * @return The id
     */
    public String getId() {
        return itemId;
    }

    @Override
    public XmlStringBuilder toXML(org.jivesoftware.smack.packet.XmlEnvironment enclosingNamespace) {
        XmlStringBuilder xml = getCommonXml();

        xml.closeEmptyElement();

        return xml;
    }

    protected final XmlStringBuilder getCommonXml() {
        XmlStringBuilder xml = new XmlStringBuilder(this);

        xml.optAttribute("id", getId());
        xml.optAttribute("node", getNode());

        return xml;
    }

    @Override
    public String toString() {
        return getClass().getName() + " | Content [" + toXML() + "]";
    }

}
