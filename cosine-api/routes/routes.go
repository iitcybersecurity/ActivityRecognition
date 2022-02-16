package routes

import (
	"webapi/controllers"

	"github.com/gofiber/fiber/v2"
)

func Setup(app *fiber.App) {
	app.Post("/api/feature/compare", controllers.Compare)
	app.Post("/api/feature/store", controllers.Store)
}
