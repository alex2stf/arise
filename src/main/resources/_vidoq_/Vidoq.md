#Vidoq
######descriptiom

## 1. Reading the docs
??TODO intro

### 1.2 Roles
#### 1.2.1 **$Device** 
#### 1.2.2 **$Implementor** 
The one that implements the design and converts static graphical content into a dynamic one.
Hadles input (requests), output (responses) and events (callbacks).
#### 1.2.3 **$Designer** 
#### 1.2.4 **$Client** 
#### 1.2.5 **$EndUser** 



## 2 Manifest
### 2.0 MVC is not UX 
Inside an Model-View-Controller design pattern, a Controller should never care about the User Experience - padding, margins, colors, fonts, sounds and general look and feel. All these attributes should be defined inside a specific `theme` or a `class`. 
A `theme` could cover general specifications while a `class` target the detail of a [graphical element](#2.1-graphic-elements).
Both of them should be defined by the [$Designer](#1.2.3-**$designer**).

### 2.1 Graphic elements
The user experience cannot be more than 3 menu items with more that 2 reprezentation types.
This is why we should have a menu (text + image) TODO, a navbar (image only) and a context menu (text only)

#### 2.1.2 Text
A text is a graphical element represented by letters. 
Font, size, color and alignment should be specified only by the [$Designer](#1.2.3-**$designer**)

#### 2.1.2 Symbols
A symbol is a fixed image icons with social accepted meaning. 
Any implementation should support a standard set of symbols.
Not all systems should provide easy access to any image in order to generate an understandable user experience, but all systems should provide access
to a fixed set of symbols . (see [image icon constants](#5.1-symbol-constants))

#### 2.1.2 Compositions
logical graphical elements used to wrap all the other grahic elements



??TODO

## 3 Data types:
### 3.1 Communication data types:
The communication between Implementor and Client is done using only 2 data types:

##### 3.1.1 BOOLEAN
- it can be represented as `1` for `true` or `0` for `false`
- any string having the value `'true'` should be considered by the Implementor as `true`

##### 3_1_2 STRING
??TODO

##### 3.1.3 NUMBER

### 3.2 Abstract data types:

#### 3.2.1 Api definitions:
##### 3.2.1.1 OPTION
##### 3.2.1.1 TABLE_CELL
##### 3.2.1.1 CHAT_ITEM


##### 3.2.1.2 HASH
##### 3.2.1.3 FUNCTION
??TODO <REPRESENTATION>

###### 3.2.1.3.1 CALLBACK 
- call a function or function type (both reflection and non reflection)
- parameter or a callback is a hash

**Callaback arguments**

###### 3.2.1.3.2 EVAL 
- eval reflection

###### 3.2.1.3.3 ROUTE 
- navigate to a page

###### 3.2.1.3.4 QUERY 
- only another query

#### 3.2.2 Visual types:
##### 3.2.2.1 WMAIN entity
##### 3.2.2.2 IMAGEICON entity

##### 3.2.2.3 DIALOG
????
###### dialog types:
- *DIALOG_OK* - ???



##### 3.2.2.4 TOAST
##### 3.2.2.5 NOTIFICATION


##### 3.2.2.6 MENU (text + imageIcon)
##### 3.2.2.7 TOOLBAR (imageIcon only)
##### 3.2.2.8 CONTEXT_OPTIONS (text only) - context menu


##### 3.2.2.7 TOOLTIP
Tooltips are meant to be exactly that, a hint or tip on what a tool or other interaction does. They are meant to clarify or help you use the content that they hover over, not add additional content:
Since tooltips are only meant to tell the purpose of an element they should be short and to the point "Click X to do X" or "User post count"
Popovers, on the other hand, can be much more verbose, they can include a header and many lines of text in the body.

##### 3.2.2.7 POPOVERS
Popovers are meant to give related additional content.
Tooltips are typically only visible on hover, for that reason if you need to be able to read the content while interacting with other parts of the page then a tooltip will not work.
Popovers are typically dismissable, whether by click on other parts of the page or second clicking the popover target (depending on implementation), for that reason you can set up a popover to allow you to interact with other elements on the page while still being able to read it's content.

On top of that, since popovers will remain open when mousing out of their target you can add additional buttons or interactions within them:


##### 3.2.2.7 PANE
##### 3.2.2.8 PAGE
##### 3.2.2.8 CHAT - special chat pane

##### 3.2.2.9 STICKER (child window)
##### 3.2.2.10 INPUT
**input types:**
###### 3.2.2.10.1 *TEXT* -???
###### 3.2.2.10.1 *PASSWORD* -???
###### 3.2.2.10.1 *FILE_CHOOSER* -???
###### 3.2.2.10.1 *RICH_TEXT* -???

##### 3.2.2.11 BUTTON
##### 3.2.2.12 TOGGLE
##### 3.2.2.13 LABEL
##### 3.2.2.14 IMAGE_ICON
##### 3.2.2.15 MEDIA (image, video) - only 2D
##### 3.2.2.16 MEDIA_RECORD
##### 3.2.2.17 CHART
    PIE
    COLUMN
    BAR
    DOUGHNUT
    
    
    The Data Viz Project is the latest of many efforts to classify and catalog different ways of visualizing data. Another excellent online resource is the Data Visualization Catalogue, developed by Severino Ribecca, which we often show in our workshops.
    https://datavizcatalogue.com/
    
##### 3.2.2.17 CUBE
##### 3.2.2.18 SPHERE
##### 3.2.2.19 VERTEX
- 3D enity



####  3.3.3 Parameter metadata:
##### 3.3.3.1 ***@Required*** 
- mandatory (`NIL` not allowed), the API is forced to be aware about the input value and is also free to throw exceptions if data is not valid

##### 3.3.3.2 ***@Extendable*** 
- required, defined as a constant with sure fallback. but Implementor can provide a platform/language specific value. 
For example, a phone device support an input type phone-number which is not a standard defined by this doc. Running a query like 
ADD_INPUT phone-number .... inside a desktop environment should display the desktop default input type.

##### 3.3.3.3 ***@Optional*** 
- is not required, (`NIL` required by fixed query syntax) but once provided the API should be aware about the input value. If is not valid, the API is free to throw errors

##### 3.3.3.4 ***@Ignorable*** 
Still optional, nothing required by syntax, the implementation API is free to ignore the input value. No errors should be thrown for theese parameter types. If the parameter is ignored, the client should be warned with the reason for the ignore. (see also Params and Warns)
The query definition parameters should map the order on theese types


## 4 Syntax
### 4.1 Data manipulation:


#### 4.1.1 Visual Manipulation syntax

---

```sql
SHOW type
```
##### params:
- type [STRING](#3_1_2-string) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element becomes displayable)

---

```sql
HIDE type
```
##### params:
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element becomes hidden)

---

```sql
VISIBLE type
```
##### params:
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element becomes visible)

---

```sql
INVISIBLE type
```
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element becomes invisible) 

---

```sql
ENABLE type
```
##### params:
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element becomes enabled)

---

```sql
DISABLE type
```
##### params:
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element becomes disabled)

---

```sql
REMOVE type
```
##### params:
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element is removed, FALSE if does not exist)

---

```sql
DRAGGABLE  type on_start on_end start_x end_x start_y end_y start_z end_z
```
##### params:
- type [STRING](#3.1.2) @Required
- on_start [CALLBACK](#3.2.1.3.1) @Optional
- on_end [CALLBACK](#3.2.1.3.1) @Optional
- start_x [NUMBER](#3.1.3) @Optional
- end_x [NUMBER](#3.1.3) @Optional
- start_y [NUMBER](#3.1.3) @Optional
- end_y [NUMBER](#3.1.3) @Optional
- start_z [NUMBER](#3.1.3) @Optional
- end_z [NUMBER](#3.1.3) @Optional
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element is draggable)

---

```sql
UNDRAGGABLE type
```
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element is removed, FALSE if does not exist)

---

```sql
ZOOMABLE type max_zoom_in max_zoom_out
```
##### params:
- type [STRING](#3.1.2) @Required
- max_zoom_in [NUMBER](#3.1.3) @Optional
- max_zoom_out [NUMBER](#3.1.3) @Optional
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element is zoomable)

---

```sql
UNZOOMABLE type
```
##### params:
- type [STRING](#3.1.2) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element is unzoomable)

---

```sql
ZOOM type val
```
##### params:
- type [STRING](#3.1.2) @Required
- val [NUMBER](#3.1.3) @Optional - percentage value
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the element is unzoomable)

---

```sql
UPDATE type property_name new_value
```
##### params:
- property_name [STRING](#3.1.2-string) @Required
- new_value [STRING](#3.1.2-string) @Required
###### returns [BOOLEAN](#BOOLEAN) (TRUE if the property is updated with success)
***Example:***
change an element position: `UPDATE my_element_ID position_x LEFT`

---

```sql
REPLACE_ID type <new_definition>
```

replace an element with a new definition reusing the specified ID except for WMAIN
Usually an element can be replaced with one by the same type. The Implementor is free to ignore a command like REPLACE inputID LABEL

INPUT_ADD_OPTION inputId optionId
INPUT_REMOVE_OPTION inputId optionId

FREEZE - disable all elements
UNFREEZE - revert to the version before FREEZE or LOADING_STATE
SHOW_LOADING_STATE - FREEZE + informing the client that something is working in progress


##### 4.1.2 Visual Interogation

```sql
IS_VISIBLE type
```

```sql
IS_HIDDEN type
```


##### 4.1.3 Visual Composition Standalone
no parent type required, no childs. parent is the application
TOAST text
ALERT <text>
DIALOG style, title, text, okAction, negativeAction
NOTIFY title text clickAction class


##### 4.1.4 Visual Composition Inherited
parent_id required. Parent is a visual component

ADD_MENU_BAR type title imageIcon
ADD_MENU_ITEM 
    type
    parentId
    text
    [clickEvent]
    [imageIcon]
    
ADD_DROPDOWN_MENU type parentId text imageIcon
returns ID

ADD_MENU_SEPARATOR parentId
returns ID

--utilities
ADD_LABEL 
    type
    parentId @Required
	text @Required
	imageIcon @Optional
	class @Optional
	gravityX @Ignorable - gravity and widht height weight can e defined by the parent panel style
	gravityY gravityZ width height

ADD_BUTTON 
type 
parentId @Required
text @Required
imageIcon @Ignorable
clickEvent @Optional
class

ADD_PANEL parentId layout class title imageIcon gravityX gravityY gravityZ width height



ADD_PAGE 
type @Required
layout @Required 
title @Optional
imageIcon @Optional
class @Optional
tooltip @Ignorable
positionX @Ignorable 
positionY @Ignorable 
positionZ @Ignorable (routable, extends panel, full page) 
rotationX @Ignorable
rotationY @Ignorable
rotationZ @Ignorable
width @Ignorable
height @Ignorable
depth @Ignorable
weight @Ignorable


ADD_STICKER
a sticker is a draggable child window with minimize option
closing a sticker is optional
closing wmain will close all stickers
a sticker cannot have pages
a sticker can have its own menu ???

tabs layout

an image menu bar is linear, no childs
ADD_IMAGE_MENU_BAR
ADD IMAGE_MENU_ITEM imageMenuBarParentId

CONTEXT MENU ??? text + imageIcon




ADD_INPUT
type
parentId
type (text, password, number, email, date, progress-bar, slider, color-select, search,
      textarea, rich-text-box, geolocation,
      checkbox, radio, file, select, checkbox-group, radio-group, file-group)

max_length
max_length
min_length
required
label_text
label_image_icon
placeholder
init_value (OPTION or VALUE)
options [list<OPTION>;optional]
on_change
bind_name property-libName
bind_on object
class

[OPTION(text, imageIcon, bind_value, class, max_length, min_length)... ]

OPTION(text, imageIcon, bind_value, class, max_length, min_length) returns 
            LABEL for select,
            RADIO for radio-group
            CHECKBOX for checkbox-group
            FILE for file-group
input does not require a validation function since basic validation should be done by type
if input typoe does not meet all necesities, any input should have 3 messages bar

SET INPUT MSG ERR inputId 'text'
SET INPUT MSG WARN inputId 'text'
SET INPUT MSG TRACE inputId 'text'
HIDE INPUT MSG inputId

##### 4.1.5 Abstract definition
DEFAULTS SET MAX_LENGTH value
             MIN_LENGTH value


DEFINE HASH (HSET) hash_name

DEFINE TOOLTIP

DEFINE POPOVER (type parent_id)

REMOVE HASH
REMOVE TOOLTIP
REMOVE POPOVER


##### 4.1.6 Abstract interogation
GET HASH (HGET) hash_name hash_property
GET HASHALL (HGETALL) hash_name returns al hash key paris in url encoded string
(implementation should be done with ease, datatype should be universal)


UPDATE HASH (HUPDATE) hash_name hash_key hash_value






CUBE -> CUBE CELL (position) -> 6 panel faces
SPHERE -> 1 panel surface
CUSTOM3D (panel surfaces list)
VERTEX (panel surface)


-- constants:

window directives:

WMAIN constant returns the type of the main window
WMAIN cannot be replaced, but it can be removed with a query like:
REMOVE WMAIN
on WMAIN removed the user interface is considered shut down






charts???


## 5 Constants:

all implementations should provide a visual represenation for the following constants:
### 5.1 Symbol constants:

### 5.1 Position constants:

gravityX LEFT CENTER RIGHT
gravityY TOP CENTER BOTTOM
gravityZ FRONT MIDDLE BEHIND
width WRAP_CONTENT | MATCH_PARENT
height WRAP_CONTENT | MATCH_PARENT


### 5.1 Pane Layout constants:
borderLayout North West Center 

grid-scrollable
grid-fixed

vertical-scrollable
vertical-fixed

horizontal-scrollable
horizontal-fixed

overlay one on top of another

---
table

layout types:
horizontal
verical
BorderLayout
![alt text](vidoq/BorderLayoutDemo.png "Logo Title Text 1")
