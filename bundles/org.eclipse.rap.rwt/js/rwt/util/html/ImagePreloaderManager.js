/*******************************************************************************
 *  Copyright: 2004, 2012 1&1 Internet AG, Germany, http://www.1und1.de,
 *                        and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Rich Ajax Platform
 ******************************************************************************/

/** This singleton manage all rwt.util.html.ImagePreloader instances. */
rwt.qx.Class.define("rwt.util.html.ImagePreloaderManager",
{
  type : "singleton",
  extend : rwt.qx.Object,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function()
  {
    this.base(arguments);

    this._objects = {};
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Adds a rwt.util.html.ImagePreloader instance to the manager
     *
     * @type member
     * @param vObject {Preloader} rwt.util.html.ImagePreloader instance
     * @return {void}
     */
    add : function(vObject) {
      this._objects[vObject.getUri()] = vObject;
    },


    /**
     * Removes a rwt.util.html.ImagePreloader instance from the manager
     *
     * @type member
     * @param vObject {Preloader} rwt.util.html.ImagePreloader instance
     * @return {void}
     */
    remove : function(vObject) {
      delete this._objects[vObject.getUri()];
    },


    /**
     * Returns whether an image preloader instance with the given source is registered
     *
     * @type member
     * @param vSource {String} Source of preloader image instance
     * @return {Boolean} whether an image preloader instance has given source
     */
    has : function(vSource) {
      return this._objects[vSource] != null;
    },


    /**
     * Return image preloader instance with given source
     *
     * @type member
     * @param vSource {String} Source of preloader image instance
     * @return {Preloader} rwt.util.html.ImagePreloader instance
     */
    get : function(vSource) {
      return this._objects[vSource];
    },


    /**
     * Create new qx.io.image.preloader instance with given source
     *
     * @type member
     * @param vSource {String} Source of preloader image instance
     * @return {Preloader} new rwt.util.html.ImagePreloader instance
     */
    create : function(vSource)
    {
      if (this._objects[vSource]) {
        return this._objects[vSource];
      }

      return new rwt.util.html.ImagePreloader(vSource);
    }
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeFields("_objects");
  }
});
