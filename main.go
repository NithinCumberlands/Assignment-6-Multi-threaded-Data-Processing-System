package main

import (
	"fmt"
	"os"
	"sync"
	"time"
)

func worker(id int, tasks <-chan string, wg *sync.WaitGroup) {
	defer wg.Done()

	fmt.Printf("Worker-%d started\n", id)

	for task := range tasks {
		fmt.Printf("Worker-%d processing: %s\n", id, task)

		time.Sleep(time.Second)

		err := writeResult(fmt.Sprintf("Worker-%d completed: %s\n", id, task))
		if err != nil {
			fmt.Println("File error:", err)
		}
	}

	fmt.Printf("Worker-%d finished\n", id)
}

func writeResult(result string) error {
	file, err := os.OpenFile("go_output.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = file.WriteString(result)
	return err
}

func main() {
	tasks := make(chan string, 10)
	var wg sync.WaitGroup

	for i := 1; i <= 3; i++ {
		wg.Add(1)
		go worker(i, tasks, &wg)
	}

	for i := 1; i <= 10; i++ {
		tasks <- fmt.Sprintf("Task-%d", i)
	}

	close(tasks)

	wg.Wait()

	fmt.Println("All tasks completed.")
}