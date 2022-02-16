package main

import (
	"fmt"
	"os"
	"webapi/routes"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
)

func main() {
	app := fiber.New()
	// Enable CORS
	app.Use(cors.New(
		cors.Config{},
	))
	routes.Setup(app)
	port := os.Getenv("PORT")
	if port == "" {
		port = "3000" // Default port if not specified
	}
	fmt.Println("Server started on port", port)
	err := app.Listen(fmt.Sprintf(":%s", port))
	if err != nil {
		fmt.Println(err)
	}
}
