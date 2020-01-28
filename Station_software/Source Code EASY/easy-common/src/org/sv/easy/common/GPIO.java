/**
 * @file //<>//
 * @brief E.A.SY. Application. This contains the methods controlling GPIO pins
 * of the main board of EASY app
 * @author Stelios Voutsinas (stevo)
 * @bug No known bugs.
 */
package org.sv.easy.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.apache.log4j.Logger;

/**
 * @brief This Class contains all the methods that are necessary for GPIO
 * control
 */
@SuppressWarnings({"ClassWithoutLogger", "ConvertToTryWithResources", "BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
public class GPIO {

    private static final Logger LOGGER = Logger.getLogger(GPIO.class);

    /**
     * @brief Reads the status of a GPIO pin
     * @param gpioPin GPIO pin to be read
     * @return The GPIO value
     */
    public String digitalRead(String gpioPin) {
        String datain = "";
        final String direction = "in";
        try {
            writeDirection(gpioPin, direction);
            RandomAccessFile gpio_pin_file = new RandomAccessFile("/sys/class/gpio/gpio" + gpioPin + "/value", "r");
            // Reset file seek pointer to read latest value of GPIO port
            gpio_pin_file.seek(0);
            char ch = gpio_pin_file.readChar();
            datain += ch;
            gpio_pin_file.close();
        } catch (Exception e) {
            LOGGER.error("Failed to open gpio file: ", e);
        }
        return datain;
    }

    /**
     * @brief Updates the status of a GPIO pin
     * @param gpioPin GPIO pin to change its state
     * @param GPIO_VAL New state
     */
    public void digitalWrite(String gpioPin, String GPIO_VAL) { //Write gpio pins #90 #91
        final String direction = "out";
        try {
            writeDirection(gpioPin, direction);
            // Open file handle to issue commands to GPIO port
            FileWriter gpio_pin_file = new FileWriter("/sys/class/gpio/gpio" + gpioPin + "/value");
            // Set GPIO value
            gpio_pin_file.write(GPIO_VAL);
            gpio_pin_file.flush();

            // Wait for a while
            java.lang.Thread.sleep(200);
            gpio_pin_file.close();
        } catch (Exception e) {
            LOGGER.error("Failed to open GPIO file: ", e);
        }
    }

    /**
     * @brief Sets a pin for I/O
     * @param gpio GPIO pin to change its state
     * @param direction I/O
     * @exception IOExeption Unable to complete the API call.
     */
    private void writeDirection(String gpio, String direction) throws IOException {
        // Open file handle to port input/output control
        FileWriter gpio_pin_direction_file = new FileWriter("/sys/class/gpio/gpio" + gpio + "/direction");
        gpio_pin_direction_file.write(direction);  // Set port for output
        gpio_pin_direction_file.flush();
        gpio_pin_direction_file.close();

    }
}
