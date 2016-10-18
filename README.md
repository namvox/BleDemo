#BLE Demo
This repo shows a power of a combination of **RxAndroidBle** and **RxBleClienMock**.

* **ScanActivity** displays a available device from **RxBleClientMock**.
* When user click to the mock device, the app will navigate to **DeviceActivity** which is supposed to allow user can type a message and send to the device.

##Send - Receive flow
Assumption: We have two devices called A and B.

* First, A writes a message to a charateristic of B. Concurrently, A listens to the characteristic which B will notify.
* Second, When B received the message on the characteristic which A sent. Then, B will notify to the listener which A has registered.
* Finally, B received a message from A, and A received a notification from B.

More details: Look at **RxBleClientMockTest**, test case **write_to_characteristic_then_notify**.


