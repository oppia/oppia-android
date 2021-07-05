package org.oppia.android.app.maven

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.Charset
import org.oppia.android.app.maven.proto.AddressBook
import org.oppia.android.app.maven.proto.Person
import org.oppia.android.app.maven.proto.Person.PhoneNumber

// This function fills in a Person message based on user input.
@Throws(IOException::class)
fun PromptForAddress(
  stdin: BufferedReader,
  stdout: PrintStream
): Person {
  val person = Person.newBuilder()
  stdout.print("Enter person ID: ")
  person.id = Integer.valueOf(stdin.readLine())
  stdout.print("Enter name: ")
  person.name = stdin.readLine()
  stdout.print("Enter email address (blank for none): ")
  val email = stdin.readLine()
  if (email.length > 0) {
    person.email = email
  }
  while (true) {
    stdout.print("Enter a phone number (or leave blank to finish): ")
    val number = stdin.readLine()
    if (number.length == 0) {
      break
    }
    val phoneNumber = PhoneNumber.newBuilder().setNumber(number)
    stdout.print("Is this a mobile, home, or work phone? ")
    val type = stdin.readLine()
    if (type == "mobile") {
      phoneNumber.type = Person.PhoneType.MOBILE
    } else if (type == "home") {
      phoneNumber.type = Person.PhoneType.HOME
    } else if (type == "work") {
      phoneNumber.type = Person.PhoneType.WORK
    } else {
      stdout.println("Unknown phone type.  Using default.")
    }
    person.addPhones(phoneNumber)
  }
  return person.build()
}

// Main function:  Reads the entire address book from a file,
//   adds one person based on user input, then writes it back out to the same
//   file.
@Throws(Exception::class)
fun main(args: Array<String>) {
  if (args.size != 1) {
    System.err.println("Usage:  AddPerson ADDRESS_BOOK_FILE")
    System.exit(-1)
  }
  val addressBook = AddressBook.newBuilder()

  // Read the existing address book.
  try {
    addressBook.mergeFrom(FileInputStream(args[0]))
  } catch (e: FileNotFoundException) {
    println(args[0] + ": File not found.  Creating a new file.")
  }

  // Add an address.
  addressBook.addPeople(
    PromptForAddress(
      BufferedReader(InputStreamReader(System.`in`)),
      System.out
    )
  )

  // Write the new address book back to disk.
  val output = FileOutputStream(args[0])
//  addressBook.build().writeTo(output)
  output.write(addressBook.build().toByteArray())
  output.close()
  println(addressBook.build())
}
