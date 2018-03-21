##########
Dashboards
##########

***************************************
Default IoT dashboard for customer user
***************************************

Tempus allow you to define default IoT dashboard for your customer users in 2 simple steps:

========================================
Step 1. Assign IoT dashboard to customer
========================================

See embedded video tutorial above on tips how to do this.

==================================
Step 2. Open customer user details
==================================

Navigate to “**Customers** -> Your customer -> **Customer Users**” and toggle edit mode using ‘pencil’ button in the top-right corner of the screen.

============================
Step 3. Select IoT dashboard
============================

Select the IoT dashboard from the list and apply changes. Please note that you can also check the “Always Fullscreen” mode to prevent a user from navigating to different dashboards/screens.

.. image:: ../_images/admin/dashboards_default.png
    :align: center
    :alt: Widget Type import windows

***************************
IoT Dashboard import/export
***************************

================
Dashboard export
================

You are able to export your dashboard to JSON format and import it to the same or another ThingsBoard instance.
In order to export dashboard, you should navigate to the Dashboards page and click on the export button located on the particular dashboard card.

.. image:: ../_images/admin/dashboards_export.png
    :align: center
    :alt: Widget Type import windows

================
Dashboard import
================

Similar, to import the dashboard you should navigate to the Dashboards page and click on the big “+” button in the bottom-right part of the screen and then click on the import button.

.. image:: ../_images/admin/dashboards_import.png
    :align: center
    :alt: Widget Type import windows

The dashboard import window should popup and you will be prompted to upload the json file.

.. image:: ../_images/admin/widget_type_import_window.png
    :align: center
    :alt: Widget Type import windows

Once you click on the “import” button you will need to specify the device aliases. This basically allows you to set what device(s) correspond to dashboard alias.

.. image:: ../_images/admin/dashboards_import_aliases.png
    :align: center
    :alt: Widget Type import windows