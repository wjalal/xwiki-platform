/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.wysiwyg.client.dom.internal;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Node;
import com.xpn.xwiki.wysiwyg.client.dom.Document;
import com.xpn.xwiki.wysiwyg.client.dom.DocumentFragment;
import com.xpn.xwiki.wysiwyg.client.dom.Range;
import com.xpn.xwiki.wysiwyg.client.dom.RangeCompare;

/**
 * The default range implementation just forwards the calls to the underlying browser implementation. It should be used
 * only for browsers that follow the W3C Range specification.
 * 
 * @version $Id$
 */
public class DefaultRange extends AbstractRange<JavaScriptObject>
{
    /**
     * Creates a new instance that wraps the given native range object. All the calls will be forwarded to this native
     * object.
     * 
     * @param jsRange The native range object to be wrapped.
     */
    DefaultRange(JavaScriptObject jsRange)
    {
        super(jsRange);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Range#isCollapsed()
     */
    public native boolean isCollapsed()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().collapsed;
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#getCommonAncestorContainer()
     */
    public native Node getCommonAncestorContainer()
    /*-{
        var range = this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()();
        return range.commonAncestorContainer;
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#getEndContainer()
     */
    public native Node getEndContainer()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().endContainer;
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#getEndOffset()
     */
    public native int getEndOffset()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().endOffset;
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#getStartContainer()
     */
    public native Node getStartContainer()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().startContainer;
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#getStartOffset()
     */
    public native int getStartOffset()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().startOffset;
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#setStart(Node, int)
     */
    public native void setStart(Node refNode, int offset)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().setStart(refNode, offset);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#setEnd(Node, int)
     */
    public native void setEnd(Node refNode, int offset)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().setEnd(refNode, offset);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#setStartBefore(Node)
     */
    public native void setStartBefore(Node refNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().setStartBefore(refNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#setStartAfter(Node)
     */
    public native void setStartAfter(Node refNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().setStartAfter(refNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#setEndBefore(Node)
     */
    public native void setEndBefore(Node refNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().setEndBefore(refNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#setEndAfter(Node)
     */
    public native void setEndAfter(Node refNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().setEndAfter(refNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#selectNode(Node)
     */
    public native void selectNode(Node refNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().selectNode(refNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#selectNodeContents(Node)
     */
    public native void selectNodeContents(Node refNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().selectNodeContents(refNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#collapse(boolean)
     */
    public native void collapse(boolean toStart)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().collapse(toStart);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#cloneContents()
     */
    public native DocumentFragment cloneContents()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().cloneContents();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#deleteContents()
     */
    public native void deleteContents()
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().deleteContents();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#extractContents()
     */
    public native DocumentFragment extractContents()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().extractContents();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#insertNode(Node)
     */
    public native void insertNode(Node newNode)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().insertNode(newNode);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#surroundContents(Node)
     */
    public native void surroundContents(Node newParent)
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().surroundContents(newParent);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractRange#compareBoundaryPoints(int, JavaScriptObject)
     */
    protected short compareBoundaryPoints(RangeCompare how, JavaScriptObject sourceRange)
    {
        return compareBoundaryPoints(how.ordinal(), sourceRange);
    }

    /**
     * Compare the boundary-points of two ranges in a document.
     * 
     * @param how The type of comparison.
     * @param sourceRange The range to compared to.
     * @return -1, 0 or 1 depending on whether the corresponding boundary-point of this range is respectively before,
     *         equal to, or after the corresponding boundary-point of sourceRange.
     */
    private native short compareBoundaryPoints(int how, JavaScriptObject sourceRange)
    /*-{
        var range = this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()();
        return range.compareBoundaryPoints(how, sourceRange);
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#cloneRange()
     */
    public Range cloneRange()
    {
        return new DefaultRange(cloneJSRange());
    }

    /**
     * @return A clone of the underlying native range object.
     */
    private native JavaScriptObject cloneJSRange()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().cloneRange();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#detach()
     */
    public native void detach()
    /*-{
        this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().detach();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#toString()
     */
    public native String toString()
    /*-{
        return this.@com.xpn.xwiki.wysiwyg.client.dom.internal.AbstractRange::getJSRange()().toString();
    }-*/;

    /**
     * {@inheritDoc}
     * 
     * @see Range#toHTML()
     */
    public String toHTML()
    {
        DocumentFragment docFragment = cloneContents();
        DivElement container = ((Document) docFragment.getOwnerDocument()).xCreateDivElement();
        container.appendChild(docFragment);
        return container.getInnerHTML();
    }
}
