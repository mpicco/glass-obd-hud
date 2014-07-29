glass-obd-hud
=============

Glass HUD for OBD data over bluetooth

This is a work in progress.  The goal at the moment is to take a date string received over bluetooth and display it on Glass.  Once the basics are working, I plan to make a heads up display of OBD data received from a Torque plugin.

This code is based on the Glass level sample.  I added bluetooth code and changed the renderer to display text.  Instead of being driven by sensor events in the original example, it's driven by receipt of OBD data over bluetooth.  

The app currently displays RPM, Speed, Throttle Position and Gear Position.