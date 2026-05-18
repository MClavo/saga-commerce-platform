import { ChevronDown } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { localDevTools } from "@/shared/local-dev-tools"

export function LocalDevToolsMenu() {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button className="h-9 px-3" type="button" variant="outline">
          Local Dev Tools
          <ChevronDown data-icon="inline-end" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-72">
        <DropdownMenuLabel>Observability & Admin</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuGroup>
          {localDevTools.map((tool) => (
            <DropdownMenuItem key={tool.href} asChild>
              <a className="flex items-center gap-3" href={tool.href} rel="noreferrer" target="_blank">
                <span className="flex size-8 shrink-0 items-center justify-center rounded-md bg-muted font-mono text-xs font-medium">
                  {tool.initials}
                </span>
                <span className="flex min-w-0 flex-col gap-0.5">
                  <span className="font-medium">{tool.name}</span>
                  <span className="truncate text-xs text-muted-foreground">{tool.description}</span>
                </span>
              </a>
            </DropdownMenuItem>
          ))}
        </DropdownMenuGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
