#!/usr/bin/env python
import math
import os
import sys

from subprocess import Popen



# Convert a list of commands into a string that is piped into the client shell script
def client_script(client_path: str, commands: [str], process_num: int):
    return "echo '" + "\n".join(commands).format(i=process_num) + "\nquit\n' | sh " + client_path


# Execute given list of commands concurrently on the given number of processes
def execute(path, commands: [str], num_processes: int):
    processes = [Popen(client_script(path, commands, i), shell=True)
                for i in range(1, num_processes + 1)]
    wait(processes)


# Execute given list of commands with one unique string in the middle of the sequence
def execute_two(path: int, commands: [str], second_commands: [str], total_processes: int):
    processes = [Popen(client_script(path, second_commands if i == math.floor(total_processes / 2) else commands, i), shell=True)
                 for i in range(1, total_processes)]
    wait(processes)


def wait(processes: [Popen]):
    for p in processes:
        p.wait()


add_setup = [
    "newflight, {i:d}, 123, 3, 250",
    "newflight, {i:d}, 124, 5, 200",
    "newflight, {i:d}, 125, 5, 200",
    "newcar, {i:d}, Cookeville, 5, 90",
    "newroom, {i:d}, Cookeville, 5, 80",
    "newcustomerid, {i:d}, 999999",
    "newcar, {i:d}, Spam, 5, 150",
    "newroom, {i:d}, Spam, 5, 100"
]
delete_setup = [
    "deleteflight, {i:d}, 123",
    "deleteflight, {i:d}, 124",
    "deleteflight, {i:d}, 125",
    "deletecar, {i:d}, Cookeville",
    "deleteroom, {i:d}, Cookeville",
    "deletecustomer, {i:d}, 999999",
    "deletecar, {i:d}, Spam",
    "deleteroom, {i:d}, Spam"
]
add_customers = [
    "newcustomerid, {i:d}, {i:d}"
]
delete_customers = [
    "deletecustomer, {i:d}, {i:d}"
]
reserve_flights = [
    "reserveflight, {i:d}, {i:d}, 123",
    "queryflight, {i:d}, 123",
    "querycustomer, {i:d}, {i:d}"
]
reserve_itinerary = [
    "itinerary, {i:d}, {i:d}, 124, 125, Cookeville, true, true",
    "queryflight, {i:d}, 124",
    "queryflight, {i:d}, 125",
    "querycar, {i:d}, Cookeville",
    "queryroom, {i:d}, Cookeville",
    "querycustomer, {i:d}, {i:d}"
]
spam_customer = [
    "querycustomer, {i:d}, 999999",
    "querycustomer, {i:d}, 999999",
    "querycustomer, {i:d}, 999999",
    "querycustomer, {i:d}, 999999",
    "querycustomer, {i:d}, 999999"
]
spam_customer_reserve = [
    "reservecar, {i:d}, 999999, Spam",
    "reserveroom, {i:d}, 999999, Spam"
]

# Expects path to client shell script
def main():
    if len(sys.argv) != 3:
        print("Error: expected 2 arguments: /path/to/client.sh, middleware-port-number")
        return
    path = os.path.abspath(sys.argv[1]) + " " + sys.argv[2]

    # Setup
    num_customers = 5
    execute(path, add_setup, 1)
    execute(path, add_customers, num_customers)

    # Reserve more seats than available 
    # (only 3 customers should get flight seats)
    input("Press enter to begin reserving flight 123...")
    execute(path, reserve_flights, num_customers)

    # Reserve itinerary with limited availablility 
    # (only 5 customers should get their itinerary)
    input("Press enter to begin reserving flight 124, flight 125, cars in Cookeville, and rooms in Cookeville...")
    execute(path, reserve_itinerary, num_customers)

    # Reserve car and room while other clients are flooding the servers with customer queries
    input("Press enter to begin spamming a querycustomer while reserving with that customer...")
    execute_two(path, spam_customer, spam_customer_reserve, num_customers)

    # cleanup
    input("Press enter to start cleanup...")
    execute(path, delete_customers, num_customers)
    execute(path, delete_setup, 1)


if __name__ == "__main__":
    main()